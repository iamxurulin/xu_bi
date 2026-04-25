package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * 单生产者类，用于向RabbitMQ队列发送消息
 */
public class SingleProducer {

    // 定义队列名称为"hello"的常量
    private final static String QUEUE_NAME = "hello";

    /**
     * 主方法，程序入口
     *
     * @param argv 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接的主机地址为本地
        factory.setHost("localhost");
        // 使用try-with-resources语句确保Connection和Channel资源被正确关闭
        try (Connection connection = factory.newConnection();  // 创建新的连接
             Channel channel = connection.createChannel()) {  // 创建通道
            // 声明队列，如果队列不存在则创建
            // 参数：队列名、是否持久化、是否独占、是否自动删除、其他参数
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            // 要发送的消息内容
            String message = "Hello World!";
            // 发布消息到队列
            // 参数：交换机名、队列名、消息属性、消息体(字节数组)
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            // 打印发送成功的日志
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}