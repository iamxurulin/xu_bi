package com.rulin.xubibackend.job.once;

import com.rulin.xubibackend.bizmq.BiMessageProducer;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图表生成任务补偿定时任务
 * 扫描长时间停留在 wait/running 状态的图表，重新发送到 MQ 队列
 */
@Component
@Slf4j
public class ChartQueueRetryJob {

    @Resource
    private ChartService chartService;

    @Resource
    private BiMessageProducer biMessageProducer;

    /** 超时阈值：180 秒（3 分钟） */
    private static final long TIMEOUT_MS = 180_000L;

    /** 每次扫描最大处理数量 */
    private static final int BATCH_SIZE = 20;

    /**
     * 每 2 分钟执行一次
     */
    @Scheduled(fixedDelay = 120_000)
    public void run() {
        try {
            log.info("[定时补偿] 开始扫描超时的图表任务");

            // 查询 wait 或 running 状态的图表
            List<Chart> pendingCharts = chartService.list()
                    .stream()
                    .filter(c -> "wait".equals(c.getStatus()) || "running".equals(c.getStatus()))
                    .filter(c -> c.getCreateTime() != null
                            && System.currentTimeMillis() - c.getCreateTime().getTime() > TIMEOUT_MS)
                    .limit(BATCH_SIZE)

                    .collect(Collectors.toList());

            if (pendingCharts.isEmpty()) {
                log.info("[定时补偿] 无超时任务");
                return;
            }

            log.info("[定时补偿] 发现 {} 个超时任务", pendingCharts.size());

            for (Chart chart : pendingCharts) {
                try {
                    // 重新发送消息到 MQ
                    biMessageProducer.sendMessage(String.valueOf(chart.getId()));
                    log.info("[定时补偿] 重新发送任务, chartId: {}", chart.getId());
                } catch (Exception e) {
                    // 发送失败：标记为 failed，避免无限重试
                    markChartFailed(chart.getId(), "补偿发送失败: " + e.getMessage());
                    log.error("[定时补偿] 重新发送失败, chartId: {}", chart.getId(), e);
                }
            }

            // 清理之前标记为超时的 running 状态，改回 wait
            pendingCharts.forEach(chart -> {
                if ("running".equals(chart.getStatus())) {
                    Chart updateChart = new Chart();
                    updateChart.setId(chart.getId());
                    updateChart.setStatus("wait");
                    updateChart.setExecMessage("任务超时，正在重新加入队列");
                    chartService.updateById(updateChart);
                }
            });

            log.info("[定时补偿] 扫描完成，处理 {} 个任务", pendingCharts.size());

        } catch (Exception e) {
            log.error("[定时补偿] 扫描异常", e);
        }
    }

    private void markChartFailed(long chartId, String message) {
        try {
            Chart updateChart = new Chart();
            updateChart.setId(chartId);
            updateChart.setStatus("failed");
            updateChart.setExecMessage(message);
            chartService.updateById(updateChart);
        } catch (Exception e) {
            log.error("[定时补偿] 标记失败状态失败, chartId: {}", chartId, e);
        }
    }
}
