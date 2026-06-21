package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 消息生产者组件，用于向RabbitMQ发送消息
 */
@Component
public class BiMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到BI队列，自动携带 x-message-id (UUID) 头用于消费者幂等
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(
                BiMqConstant.BI_EXCHANGE_NAME,
                BiMqConstant.BI_ROUTING_KEY,
                message,
                BiMqConfig.messageIdProcessor()
        );
    }
}
