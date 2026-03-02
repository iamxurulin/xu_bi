package com.rulin.xubibackend.mq;

import com.rabbitmq.client.*;

/**
 * DirectConsumer类演示了RabbitMQ中direct类型的交换器(Exchange)的使用
 * 创建了两个队列，分别绑定到同一个交换器上，并使用不同的路由键
 */
public class DirectConsumer {

    // 定义交换器的名称，常量
    private static final String EXCHANGE_NAME = "direct_exchange";

    /**
     * 主方法，创建消费者，监听两个队列
     * @param argv 命令行参数（未使用）
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接主机为本地
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明一个direct类型的交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 声明第一个队列名称
        String queueName = "ruhuo_queue";
        // 声明队列，参数：队列名、持久化、非独占、非自动删除、额外参数
        channel.queueDeclare(queueName, true, false, false, null);
        // 将队列与交换器绑定，路由键为"ruhuo"
        channel.queueBind(queueName, EXCHANGE_NAME, "ruhuo");

        // 声明第二个队列名称
        String queueName2 = "rushan_queue";
        // 声明第二个队列
        channel.queueDeclare(queueName2, true, false, false, null);
        // 将第二个队列与交换器绑定，路由键为"rushan"
        channel.queueBind(queueName2, EXCHANGE_NAME, "rushan");

        // 打印等待消息的提示信息
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义第一个队列的消费者回调
        DeliverCallback ruhuodeliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), "UTF-8");
            // 打印接收到的消息和路由键
            System.out.println(" [ruhuo] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 定义第二个队列的消费者回调
        DeliverCallback rushandeliverCallback = (consumerTag, delivery) -> {
            // 将消息体转换为字符串
            String message = new String(delivery.getBody(), "UTF-8");
            // 打印接收到的消息和路由键
            System.out.println(" [rushan] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 开始消费第一个队列的消息，自动确认
        channel.basicConsume(queueName, true, ruhuodeliverCallback, consumerTag -> {
        });
        // 开始消费第二个队列的消息，自动确认
        channel.basicConsume(queueName, true, rushandeliverCallback, consumerTag -> {
        });

    }
}