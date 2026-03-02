package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * FanoutProducer类用于演示RabbitMQ中的发布/订阅模式
 * 通过fanout类型的交换机向所有队列广播消息
 */
public class FanoutProducer {

  // 定义交换机的名称
  private static final String EXCHANGE_NAME = "fanout-exchange";

  /**
   * 主方法，用于创建生产者并发送消息
   * @param argv 命令行参数（本程序中未使用）
   * @throws Exception 可能抛出的异常
   */
  public static void main(String[] argv) throws Exception {
    // 创建连接工厂
    ConnectionFactory factory = new ConnectionFactory();
    // 设置RabbitMQ服务器的主机地址
    factory.setHost("localhost");
    // 使用try-with-resources语句确保资源被正确关闭
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        // 声明一个fanout类型的交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        // 创建Scanner对象用于从控制台读取输入
        Scanner scanner = new Scanner(System.in);

        // 循环读取用户输入，直到没有输入为止
        while (scanner.hasNext()) {
            // 读取用户输入的消息
            String message = scanner.nextLine();

            // 向交换机发送消息，fanout类型交换机会忽略路由键
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            // 打印已发送的消息
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
  }
}