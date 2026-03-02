package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

/**
 * 多生产者类，用于向RabbitMQ队列发送消息
 * 该程序创建一个持久化的队列，并允许用户通过控制台输入消息发送到队列
 */
public class MultiProducer {

    // 定义队列名称常量
    private static final String TASK_QUEUE_NAME = "multi_queue";

    /**
     * 主方法，程序入口
     * @param argv 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置RabbitMQ服务器主机地址
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明一个持久化的队列
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            // 创建Scanner对象用于读取控制台输入
            Scanner scanner = new Scanner(System.in);

            // 循环读取用户输入，直到没有输入为止
            while (scanner.hasNext()) {
                // 读取用户输入的消息
                String message = scanner.nextLine();
                // 发布消息到队列，设置消息为持久化
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                // 打印已发送的消息
                System.out.println(" [x] Sent '" + message + "'");
            }

        }
    }

}