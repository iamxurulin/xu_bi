package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * TTL (Time-To-Live) 消费者示例类
 * 该类演示了如何创建一个带有消息TTL(Time-To-Live)功能的队列并消费消息
 */
public class TtlConsumer {

    // 定义队列名称常量
    private final static String QUEUE_NAME = "ttl_queue";

    /**
     * 主方法，创建消费者连接并消费TTL队列中的消息
     * @param argv 命令行参数（未使用）
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置RabbitMQ服务器主机地址
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建信道
        Channel channel = connection.createChannel();

        // 创建参数Map，用于设置队列属性
        Map<String,Object> args = new HashMap<String,Object>();
        // 设置消息TTL为5000毫秒（5秒）
        args.put("x-message-ttl",5000);

        // 声明队列，并设置TTL参数
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        // 打印等待消息提示
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 创建消息交付回调
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // 打印接收到的消息
            System.out.println(" [x] Received '" + message + "'");
        };
        // 开始消费队列中的消息
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}