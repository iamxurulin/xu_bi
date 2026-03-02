package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

/**
 * 单消费者类，用于从RabbitMQ队列中消费消息
 */
public class SingleConsumer {

    // 定义队列名称为"hello"的常量
    private final static String QUEUE_NAME = "hello";

    /**
     * 主方法，连接RabbitMQ并消费消息
     * @param argv 命令行参数（未使用）
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置RabbitMQ服务器主机地址为本地
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明队列，参数分别为：队列名称、是否持久化、是否独占、是否自动删除、其他参数
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 打印等待消息的提示信息
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 创建消息传递回调接口的实现
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // 将接收到的消息体转换为UTF-8字符串
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // 打印接收到的消息
            System.out.println(" [x] Received '" + message + "'");
        };
        // 开始消费队列中的消息，参数分别为：队列名称、是否自动确认、消息传递回调、消费者标签回调
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}