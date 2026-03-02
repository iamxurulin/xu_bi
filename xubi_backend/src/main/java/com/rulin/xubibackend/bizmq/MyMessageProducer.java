package com.rulin.xubibackend.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息生产者组件类
 * 用于向RabbitMQ发送消息
 */
@Component
public class MyMessageProducer {

    /**
     * RabbitMQ模板类，用于发送消息
     * 通过@Resource注解注入，实现依赖注入
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息的方法
     * @param exchange 交换机名称，用于指定消息发送到哪个交换机
     * @param routingKey 路由键，用于确定消息的路由规则
     * @param message 要发送的消息内容
     */
    public void sendMessage(String exchange,String routingKey,String message){
        // 使用RabbitTemplate的convertAndSend方法发送消息
        // 参数包括交换机名称、路由键和消息内容
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
