package com.rulin.xubibackend.bizmq;

import com.rulin.xubibackend.model.entity.Chart;
import com.rulin.xubibackend.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 死信队列验证测试
 * 直接往 bi_dlq 队列发消息，验证死信消费者能否正确将 chart 标记为 failed
 */
@SpringBootTest
class BiMqDeadLetterTest {

    @Resource
    private ChartService chartService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void testDeadLetterConsumerMarkChartAsFailed() {
        // 1. 创建一个状态为 running 的图表，模拟一个卡住的异步任务
        Chart chart = new Chart();
        chart.setName("死信队列测试");
        chart.setGoal("测试死信队列消费者");
        chart.setStatus("running");
        chart.setUserId(2020743781955637249L);
        boolean saveResult = chartService.save(chart);
        assertTrue(saveResult);

        long chartId = chart.getId();

        // 2. 验证初始状态是 running
        Chart existing = chartService.getById(chartId);
        assertNotNull(existing);
        assertEquals("running", existing.getStatus());

        // 3. 往死信队列发消息
        String deadLetterMessage = String.valueOf(chartId);
        rabbitTemplate.convertAndSend("bi_dlx_exchange", "bi_dlk", deadLetterMessage);

        // 4. 等待死信消费者处理（等待 3 秒）
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. 验证 chart 状态已被标记为 failed
        Chart updated = chartService.getById(chartId);
        assertNotNull(updated);
        assertEquals("failed", updated.getStatus());
        assertEquals("消息重试耗尽，已转入死信队列", updated.getExecMessage());
    }
}
