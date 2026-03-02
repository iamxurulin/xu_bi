package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.manager.AiManager;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息消费者组件，负责处理与BI相关的消息
 * 使用RabbitMQ作为消息队列，处理图表生成相关的任务
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;  // 图表服务，用于数据库操作

    @Resource
    private AiManager aiManager;  // AI管理器，用于与AI服务交互

    /**
     * 处理来自RabbitMQ的消息
     * @param message 接收到的消息内容，包含图表ID
     * @param channel RabbitMQ通道，用于消息确认
     * @param deliveryTag 消息投递标签，用于消息确认
     * @throws Exception 可能抛出异常
     */
    /**
     * 使用@SneakyThrows注解来简化异常处理
     * 该注解会自动将被注解方法内抛出的受检异常转换为未检查异常
     * 这样可以避免在方法签名中声明throws子句
     * 通常用于那些确定不会发生或者可以安全忽略的异常
     * 属于Lombok库提供的注解之一
     */
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        // 记录接收到的消息
        log.info("receieveMessage message = {}", message);
        // 检查消息是否为空
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        // 解析消息获取图表ID
        long chartId = Long.parseLong(message);
        // 从数据库获取图表信息
        Chart chart = chartService.getById(chartId);

        // 检查图表是否存在
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }

        // 更新图表状态为"running"
        Chart updateChart = new Chart();
// 更新图表信息，设置图表ID和状态为"running"
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
// 调用chartService的updateById方法更新图表，并获取更新结果
        boolean b = chartService.updateById(updateChart);

        // 检查状态更新是否成功
        if (!b) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
            return;
        }

        // 构建用户输入并发送给AI服务
        String result = aiManager.sendMsgToXunFeiSpark(true, buildUserInput(chart));

        // 解析AI返回的结果
        String[] splits = result.split("【【【【");

        // 检查AI返回结果格式是否正确
        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "AI 生成错误");
            return;
        }

        // 提取图表数据和结果
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        // 更新图表结果
        // 创建一个新的Chart对象用于更新图表结果
        Chart updateChartResult = new Chart();
        // 设置更新后的图表ID，保持与原图表ID一致
        updateChartResult.setId(chart.getId());
        // 设置生成的图表数据
        updateChartResult.setGenChart(genChart);
        // 设置生成的结果信息
        updateChartResult.setGenResult(genResult);
        // 设置图表状态为"succeed"，表示更新成功
        updateChartResult.setStatus("succeed");

        // 检查结果更新是否成功
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }

        // 手动确认消息处理成功
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建发送给AI的用户输入
     *
     * @param chart 图表对象，包含目标、类型和数据
     * @return 格式化的用户输入字符串
     */
    private String buildUserInput(Chart chart) {
        // 从图表对象中获取目标、类型和数据
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 构建输入字符串
        StringBuilder userInput = new StringBuilder();
        // 添加分析需求
        userInput.append("分析需求：").append("\n");

        String userGoal = goal;

        // 如果指定了图表类型，添加到目标中
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ".请使用" + chartType;
        }

        // 添加用户目标
        userInput.append(userGoal).append("\n");
        // 添加原始数据
        userInput.append("原始数据：").append("\n");
        // 添加CSV数据
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    /**
     * 处理图表更新错误
     * 该方法用于在图表更新过程中发生错误时，将图表状态更新为失败，并记录错误信息
     *
     * @param chartId     图表ID，用于标识需要更新的图表
     * @param execMessage 错误信息，用于记录失败的具体原因
     */
    private void handleChartUpdateError(long chartId, String execMessage) {

        /**
         * 创建一个新的Chart对象用于更新图表状态
         * 设置图表ID、状态为"failed"以及执行消息
         * 然后尝试更新数据库中的图表记录
         */
        Chart updateChartResult = new Chart();  // 创建Chart对象
        updateChartResult.setId(chartId);        // 设置图表ID
        updateChartResult.setStatus("failed");   // 设置图表状态为失败
        updateChartResult.setExecMessage(execMessage);  // 设置执行消息
        // 调用chartService的updateById方法更新图表记录
        boolean updateResult = chartService.updateById(updateChartResult);
        // 如果更新失败，记录错误日志
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
