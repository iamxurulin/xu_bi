package com.rulin.xubibackend.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * TTL生产者类
 * 该类用于发送带有TTL(Time-To-Live)消息的RabbitMQ生产者
 */
public class TtlProducer {

    // 定义队列名称常量
    private final static String QUEUE_NAME = "ttl_queue";

    /**
     * 主方法
     * @param argv 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接主机地址
        factory.setHost("localhost");
        // 使用try-with-resources语句确保资源正确关闭
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            // 要发送的消息内容
            String message = "Hello World!";

            // 创建带有TTL属性的消息属性
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    // 设置消息过期时间为1000毫秒(1秒)
                    .expiration("1000")
                            .build();
            // 发布消息到指定交换机和路由键，并设置消息属性
            channel.basicPublish("my-exchange", "routing-key",properties,  message.getBytes(StandardCharsets.UTF_8));
            // 打印发送消息的日志
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}