package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * TopicProducer类用于实现一个主题交换机(Exchange)的生产者(Producer)
 * 该生产器允许用户通过控制台输入消息和路由键，并将消息发送到指定的主题交换机
 */
public class TopicProducer {

  // 定义交换机的名称为"topic_exchange"
  private static final String EXCHANGE_NAME = "topic_exchange";

  /**
   * 主方法，程序的入口点
   * @param argv 命令行参数
   * @throws Exception 可能抛出的异常
   */
  public static void main(String[] argv) throws Exception {
    // 创建连接工厂
    ConnectionFactory factory = new ConnectionFactory();
    // 设置连接的主机为本地
    factory.setHost("localhost");
    // 使用try-with-resources语句确保连接和通道在使用后被正确关闭
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {

        // 声明一个类型为"topic"的交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 注释掉的代码原本用于从命令行参数获取路由键和消息
//        String routingKey = getRouting(argv);
//        String message = getMessage(argv);

        // 创建Scanner对象用于读取用户控制台输入
        Scanner scanner = new Scanner(System.in);

        // 循环读取用户输入，直到没有输入为止
        while (scanner.hasNext()) {

            // 读取一行用户输入
            String userInput = scanner.nextLine();
            // 将输入按空格分割成字符串数组
            String[] strings = userInput.split(" ");

            // 如果输入数组长度小于1，跳过此次循环
            if(strings.length<1){
                continue;
            }
            // 从输入数组中提取消息和路由键
            String message = strings[0];
            String routingKey = strings[1];

            // 将消息发布到指定的交换机和路由键
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            // 打印发送的消息和路由键
            System.out.println(" [x] Sent '" + message + " with routing: " + routingKey + "'");
        }

    }
  }
  //..
}