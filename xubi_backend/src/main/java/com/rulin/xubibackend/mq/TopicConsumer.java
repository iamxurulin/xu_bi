package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * TopicConsumer类是一个基于RabbitMQ的主题(Topic)交换机模式的消费者示例。
 * 它创建了三个不同的队列，分别用于接收前端、后端和产品相关的消息。
 */
public class TopicConsumer {

    // 定义交换机的名称，使用主题交换机模式
    private static final String EXCHANGE_NAME = "topic_exchange";

    /**
     * 程序的主入口方法
     * @param argv 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂，设置RabbitMQ服务器的主机地址为本地
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建通道
        Channel channel = connection.createChannel();

        // 声明一个主题类型的交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 声明前端队列，并设置持久化
        String queueName = "frontend_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        // 将前端队列与交换机绑定，使用路由键"#.前端.#"匹配所有包含"前端"的消息
        channel.queueBind(queueName, EXCHANGE_NAME, "#.前端.#");

        // 声明后端队列，并设置持久化
        String queueName2 = "backend_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        // 将后端队列与交换机绑定，使用路由键"#.后端.#"匹配所有包含"后端"的消息
        channel.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");

        // 声明产品队列，并设置持久化
        String queueName3 = "product_queue";
        channel.queueDeclare(queueName3, true, false, false, null);
        // 将产品队列与交换机绑定，使用路由键"#.产品.#"匹配所有包含"产品"的消息
        channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");


        // 打印等待消息的提示信息
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义前端消息的回调函数
        DeliverCallback fengdeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [feng] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 定义后端消息的回调函数
        DeliverCallback lindeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [lin] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 定义产品消息的回调函数
        DeliverCallback huodeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [huo] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 开始消费前端队列中的消息
        channel.basicConsume(queueName, true, fengdeliverCallback, consumerTag -> {
        });
        // 开始消费后端队列中的消息
        channel.basicConsume(queueName2, true, lindeliverCallback, consumerTag -> {
        });
        // 开始消费产品队列中的消息
        channel.basicConsume(queueName3, true, huodeliverCallback, consumerTag -> {
        });

    }
}