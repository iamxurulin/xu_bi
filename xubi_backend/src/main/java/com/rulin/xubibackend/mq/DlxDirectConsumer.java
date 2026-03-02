package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列消费者示例类
 * 该类实现了两个队列(dog_queue和cat_queue)的消费者，这些队列配置了死信交换机
 */
public class DlxDirectConsumer {

    // 死信交换机的名称常量
    private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";
    // 工作交换机的名称常量
    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";
    /**
     * 主方法，创建连接和通道，设置交换机和队列，并启动消费者
     * @param argv 命令行参数（未使用）
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建通道
        Channel channel = connection.createChannel();
        // 声明工作交换机，类型为direct
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        // 创建第一个队列的参数，配置死信交换机和路由键
        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key","waibao");

        // 声明第一个队列(dog_queue)，并设置参数
        String queueName = "dog_queue";
        channel.queueDeclare(queueName, true, false, false, args);
        // 将队列与交换机绑定，路由键为"dog"
        channel.queueBind(queueName, WORK_EXCHANGE_NAME, "dog");

        // 创建第二个队列的参数，配置死信交换机和路由键
        Map<String,Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key","boss");

        // 声明第二个队列(cat_queue)，并设置参数
        String queueName2 = "cat_queue";
        channel.queueDeclare(queueName2, true, false, false, args2);
        // 将队列与交换机绑定，路由键为"cat"
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "cat");

        // 打印等待消息的提示信息
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // 定义第一个队列的消费者回调，拒绝所有消息
        DeliverCallback ruleideliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息，不重新入队
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [dog] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 定义第二个队列的消费者回调，拒绝所有消息
        DeliverCallback ruyindeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息，不重新入队
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [cat] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        // 开始消费第一个队列，不自动确认消息
        channel.basicConsume(queueName, false, ruleideliverCallback, consumerTag -> {
        });
        // 开始消费第二个队列，不自动确认消息
        channel.basicConsume(queueName2, false, ruyindeliverCallback, consumerTag -> {
        });

    }
}