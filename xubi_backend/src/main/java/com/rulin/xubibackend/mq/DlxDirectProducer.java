package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

/**
 * 死信队列生产者示例类
 * 该类演示了如何创建一个生产者，向工作交换器发送消息，
 * 这些消息在未被正确处理时会进入死信队列
 */
public class DlxDirectProducer {

    // 死信交换器的名称常量
    private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";
    // 工作交换器的名称常量
    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

    /**
     * 主方法，创建生产者并发送消息
     * @param argv 命令行参数（未使用）
     * @throws Exception 可能抛出的异常
     */
  public static void main(String[] argv) throws Exception {
        // 创建连接工厂
    ConnectionFactory factory = new ConnectionFactory();
        // 设置连接主机为本地
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
            // 声明死信交换器，类型为direct
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

            // 声明boss队列并绑定到死信交换器
        String queueName = "boss_dlx_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "boss");

            // 声明waibao队列并绑定到死信交换器
        String queueName2 = "waibao_dlx_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

            // 定义boss队列的消费者回调，接收消息后拒绝确认
        DeliverCallback bossdeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
                // 拒绝消息确认，使消息进入死信队列
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [boss] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

            // 定义waibao队列的消费者回调，接收消息后拒绝确认
        DeliverCallback waibaodeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
                // 拒绝消息确认，使消息进入死信队列
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [waibao] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

            // 开始消费boss队列
        channel.basicConsume(queueName, false, bossdeliverCallback, consumerTag -> {
        });
            // 开始消费waibao队列
        channel.basicConsume(queueName2, false, waibaodeliverCallback, consumerTag -> {
        });
            // 创建Scanner用于读取用户输入
        Scanner scanner = new Scanner(System.in);

            // 循环读取用户输入并发送消息
        while (scanner.hasNext()) {
            String userInput = scanner.nextLine();
            String[] strings = userInput.split(" ");

                // 检查输入格式是否正确
            if(strings.length<1){
                continue;
            }
            String message = strings[0];
            String routingKey = strings[1];

                // 向工作交换器发送消息
            channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing: " + routingKey + "'");
        }

    }
  }
  //..
}