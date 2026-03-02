package com.rulin.xubibackend.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息生产者组件，用于向RabbitMQ发送消息
 * 该类被标记为Spring的组件，会被Spring容器自动管理
 */
@Component
public class BiMessageProducer {

    /**
     * RabbitTemplate实例，用于执行实际的RabbitMQ消息发送操作
     * 通过@Resource注解自动注入，由Spring容器提供
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息的方法
     *该方法使用注入的RabbitTemplate将消息发送到指定的交换机和路由键
     * @param message 要发送的消息内容
     *
     */
    public void sendMessage(String message) {
        // 使用convertAndSend方法发送消息，指定交换机名称、路由键和消息内容
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY, message);
    }
}
