package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * FanoutConsumer类演示了RabbitMQ中扇出(Fanout)交换机的使用
 * 扇出交换机会将消息广播到所有绑定的队列
 */
public class FanoutConsumer {
  // 定义交换机的名称常量
  private static final String EXCHANGE_NAME = "fanout-exchange";

  public static void main(String[] argv) throws Exception {
    // 创建连接工厂
    ConnectionFactory factory = new ConnectionFactory();
    // 设置RabbitMQ服务器主机地址
    factory.setHost("localhost");
    // 创建连接
    Connection connection = factory.newConnection();
    // 创建第一个通道
    Channel channel1 = connection.createChannel();
    // 创建第二个通道
    Channel channel2 = connection.createChannel();

    // 声明一个扇出类型的交换机
    channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
    // 声明第一个队列
    String queueName = "rufeng_queue";
    channel1.queueDeclare(queueName,true,false,false,null);
    // 将队列与交换机绑定
    channel1.queueBind(queueName, EXCHANGE_NAME, "");

    // 声明第二个队列
    String queueName2 = "rulin_queue";
    channel2.queueDeclare(queueName2,true,false,false,null);
    // 将第二个队列与交换机绑定（绑定了两次，演示多重绑定）
    channel2.queueBind(queueName2, EXCHANGE_NAME, "");
    channel2.queueBind(queueName2, EXCHANGE_NAME, "");

    // 打印等待消息的提示信息
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    // 定义第一个队列的消费者回调函数
    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        // 将消息体转换为字符串
        String message = new String(delivery.getBody(), "UTF-8");
        // 打印接收到的消息，[风]标识消息来源
        System.out.println(" [风] Received '" + message + "'");
    };

    // 定义第二个队列的消费者回调函数
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
      // 将消息体转换为字符串
      String message = new String(delivery.getBody(), "UTF-8");
      // 打印接收到的消息，[林]标识消息来源
      System.out.println(" [林] Received '" + message + "'");
    };

    // 开始消费第一个队列中的消息
    channel1.basicConsume(queueName, true, deliverCallback1, consumerTag -> { });
    // 开始消费第二个队列中的消息
    channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });

  }
}