package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.manager.AiManager;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import com.rulin.xubibackend.validator.ChartValidator;
import com.rulin.xubibackend.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RAtomicLong;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * BI 消息消费者，负责处理图表生成任务
 * 包含：幂等性检查、AI 结果校验、Redis 重试计数、死信队列处理
 */
@Component
@Slf4j
public class BiMessageConsumer {

    private static final int MAX_RETRY_COUNT = 3;

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @Resource
    private ChartValidator chartValidator;

    @Resource
    private org.redisson.api.RedissonClient redissonClient;

    /**
     * 处理 BI 图表生成消息
     * ackMode = "MANUAL"
     * 重试次数存储于 Redis RAtomicLong，重试耗尽后 nack 走死信队列
     */
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId) throws IOException {

        // === 幂等性检查 ===
        if (StringUtils.isNotBlank(messageId)) {
            String idempotentKey = "mq_msg:" + messageId;
            if (redissonClient.getBucket(idempotentKey).trySet("1", 300, java.util.concurrent.TimeUnit.SECONDS)) {
                log.info("幂等检查通过, messageId: {}", messageId);
            } else {
                log.info("重复消息，跳过处理, messageId: {}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }
        }

        long chartId = 0L;
        try {
            chartId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            log.error("消息格式错误: {}", message);
            channel.basicNack(deliveryTag, false, false);
            return;
        }

        // 获取当前重试次数
        RAtomicLong retryCounter = redissonClient.getAtomicLong("mq_retry:" + chartId);
        long currentRetry = retryCounter.incrementAndGet();

        log.info("BI 消费消息, chartId: {}, retryCount: {}/{}", chartId, currentRetry, MAX_RETRY_COUNT);

        try {
            doProcess(chartId, channel, deliveryTag);
            channel.basicAck(deliveryTag, false);
            retryCounter.delete(); // 处理成功，清理重试计数
            log.info("BI 消息处理成功, chartId: {}", chartId);

        } catch (Exception e) {
            log.error("BI 消息处理异常, chartId: {}", chartId, e);
            if (currentRetry < MAX_RETRY_COUNT) {
                // 未达到最大重试次数，nack 重入队列
                channel.basicNack(deliveryTag, false, true);
                log.warn("消息重入队列，chartId: {}, retry: {}/{}", chartId, currentRetry, MAX_RETRY_COUNT);
            } else {
                // 重试耗尽，nack 走死信队列
                channel.basicNack(deliveryTag, false, false);
                log.error("消息重试耗尽，转入死信队列, chartId: {}", chartId);
            }
        }
    }

    /**
     * 核心处理逻辑（含 LLM-as-Judge 二次校验回环）
     */
    private void doProcess(long chartId, Channel channel, long deliveryTag) {
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            handleChartUpdateError(chartId, "图表不存在");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表不存在: " + chartId);
        }

        // 更新状态为 running
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean updateOk = chartService.updateById(updateChart);
        if (!updateOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表执行中状态失败");
        }

        String userInput = buildUserInput(chart);

        // 首次 AI 调用
        String aiResult = aiManager.sendMsgToXunFeiSpark(true, userInput);
        String[] splits = parseAiResult(aiResult);
        String genChart = splits[0];
        String genResult = splits[1];

        // === LLM-as-Judge 二次校验回环 ===
        int maxRetries = 2;
        for (int retry = 0; retry <= maxRetries; retry++) {
            ValidationResult vr = chartValidator.validate(genChart, genResult);
            if (vr.isValid()) {
                break; // 校验通过
            }

            if (retry >= maxRetries) {
                handleChartUpdateError(chartId, "图表校验失败: " + vr.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成结果校验失败，已重试 " + maxRetries + " 次: " + vr.getMessage());
            }

            log.warn("LLM-as-Judge 第 {} 次重试, 校验失败: {}", retry + 1, vr.getMessage());
            handleChartUpdateError(chartId, "LLM-as-Judge 正在重试第 " + (retry + 1) + " 次, 原因: " + vr.getMessage());

            aiResult = aiManager.retryChartGeneration(genChart, vr.getMessage(), userInput);
            splits = parseAiResult(aiResult);
            genChart = splits[0];
            genResult = splits[1];
        }

        // 保存结果
        Chart updateResult = new Chart();
        updateResult.setId(chart.getId());
        updateResult.setGenChart(genChart);
        updateResult.setGenResult(genResult);
        updateResult.setStatus("succeed");
        boolean saveOk = chartService.updateById(updateResult);
        if (!saveOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新图表结果失败");
        }
    }

    /**
     * 解析 AI 返回结果，返回 [genChart, genResult]
     */
    private String[] parseAiResult(String aiResult) {
        String[] splits = aiResult.split("【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成结果格式错误");
        }
        return new String[]{cleanAiOutput(splits[1].trim()), cleanAiOutput(splits[2].trim())};
    }

    private String cleanAiOutput(String raw) {
        String s = raw.trim();
        s = s.replaceAll("^```\\w*\\s*|\\s*```$", "");
        while (s.length() > 0 && (s.startsWith("'") || s.startsWith("`") || s.startsWith("\""))) {
            s = s.substring(1);
        }
        while (s.length() > 0 && (s.endsWith("'") || s.endsWith("`") || s.endsWith("\""))) {
            s = s.substring(0, s.length() - 1);
        }
        return s.trim();
    }

    /**
     * 死信队列消费者
     */
    @RabbitListener(queues = {"bi_dlq"}, ackMode = "MANUAL", containerFactory = "dlqListenerContainerFactory")
    public void handleDeadLetterMessage(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.error("=== BI 死信队列收到消息: {} ===", message);
        channel.basicAck(deliveryTag, false);

        try {
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if (chart != null && "running".equals(chart.getStatus())) {
                Chart updateChart = new Chart();
                updateChart.setId(chartId);
                updateChart.setStatus("failed");
                updateChart.setExecMessage("消息重试耗尽，已转入死信队列");
                chartService.updateById(updateChart);
                log.info("死信消费者已将 chartId {} 标记为 failed", chartId);
            }
        } catch (Exception e) {
            log.error("死信消费者无法解析消息: {}", message);
        }
    }

    private String buildUserInput(Chart chart) {
        StringBuilder sb = new StringBuilder();
        sb.append("分析需求：\n");
        String goal = chart.getGoal();
        if (StringUtils.isNotBlank(chart.getChartType())) {
            goal += ".请使用" + chart.getChartType();
        }
        sb.append(goal).append("\n");
        sb.append("原始数据：\n");
        sb.append(chart.getChartData()).append("\n");
        sb.append("注意：请根据表头和数据自行判断数值的单位（如万元、亿元、元、%等），分析结论中的金额单位应与原始数据保持一致，不要随意换算。\n");
        return sb.toString();
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean ok = chartService.updateById(updateChart);
        if (!ok) {
            log.error("更新图表失败状态失败, chartId: {}, message: {}", chartId, execMessage);
        }
    }
}
