package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * 多消费者示例类
 * 演示了RabbitMQ中多个消费者从同一个队列消费消息的场景
 */
public class MultiConsumer {

    // 定义任务队列的名称
    private static final String TASK_QUEUE_NAME = "multi_queue";

    /**
     * 程序入口点
     * @param argv 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] argv) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置RabbitMQ服务器主机地址
        factory.setHost("localhost");
        // 创建连接
        final Connection connection = factory.newConnection();

        // 创建两个消费者
        for (int i = 0; i < 2; i++) {
            // 为每个消费者创建一个通道
            final Channel channel = connection.createChannel();

            // 声明队列，设置为持久化
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // 设置预取计数为1，确保每个消费者一次只处理一条消息
            channel.basicQos(1);

            // 为了在lambda表达式中使用i变量，创建一个final副本
            int finalI = i;

            // 创建消息交付回调
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // 将消息体转换为字符串
                String message = new String(delivery.getBody(), "UTF-8");

                // 使用try-catch-finally块处理消息接收和确认逻辑
                try {

                    // 打印接收到的消息内容
                    System.out.println(" [x] Received '" + message + "'");
                    // 手动确认消息已被成功处理，不批量确认
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                    // 模拟消息处理耗时20秒
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    // 打印中断异常堆栈信息
                    e.printStackTrace();
                    // 如果消息处理被中断，拒绝该消息且不重新入队
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                } finally {
                    // 无论处理成功还是失败，都打印完成信息
                    System.out.println(" [x] Done");
                    // 在finally块中再次确认消息处理完成
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            // 开始消费队列中的消息，第二个参数false表示手动确认消息
            // deliverCallback是处理消息的回调函数
            // consumerTag用于取消消费者时的标识
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });

        }
    }
}