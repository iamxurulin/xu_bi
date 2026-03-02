package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 消息消费者组件
 * 使用@Component注解将此类标记为Spring组件
 * 使用@Slf4j注解自动生成日志对象
 */
@Component
@Slf4j
public class MyMessageConsumer {

    /**
     * 消息接收方法
     * @SneakyThrows注解用于简化异常处理
     * @RabbitListener注解标记此方法为消息监听器，监听名为"code_queue"的队列
     * ackMode设置为"MANUAL"，表示手动消息确认
     * 
     * @param message 接收到的消息内容
     * @param channel RabbitMQ通道对象，用于后续操作
     * @param deliveryTag 消息投递标签，用于唯一标识消息
     */
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void receveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        // 打印接收到的消息日志
        log.info("receiveMessage message = {}",message);
        // 手动确认消息已被成功处理
        channel.basicAck(deliveryTag,false);
    }
}
