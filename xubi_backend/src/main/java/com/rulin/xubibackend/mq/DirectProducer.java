package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * DirectProducer类是一个直接交换机(Direct Exchange)的生产者示例
 * 它通过命令行输入发送消息到指定的路由键
 */
public class DirectProducer {

  // 定义交换机的名称为"direct_exchange"
  private static final String EXCHANGE_NAME = "direct_exchange";

  /**
   * 主方法，程序入口点
   * @param argv 命令行参数
   * @throws Exception 可能抛出的异常
   */
  public static void main(String[] argv) throws Exception {
    // 创建连接工厂对象
    ConnectionFactory factory = new ConnectionFactory();
    // 设置RabbitMQ服务器的主机地址为本地
    factory.setHost("localhost");


    // 使用try-with-resources语句确保资源被正确关闭
    try (Connection connection = factory.newConnection();  // 创建连接
         Channel channel = connection.createChannel()) {    // 创建通道
        // 声明一个类型为direct的交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 创建Scanner对象用于从控制台读取用户输入
        Scanner scanner = new Scanner(System.in);

        // 循环读取用户输入直到没有更多输入
        while (scanner.hasNext()) {
            // 读取一行用户输入
            String userInput = scanner.nextLine();
            // 将输入按空格分割成字符串数组
            String[] strings = userInput.split(" ");

            // 检查输入是否有效，至少包含一个元素
            if(strings.length<1){
                continue;  // 如果输入无效，跳过当前循环
            }
            // 获取消息内容（输入的第一个元素）
            String message = strings[0];
            // 获取路由键（输入的第二个元素）
            String routingKey = strings[1];

            // 发布消息到指定的交换器和路由键
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            // 打印发送消息的信息
            System.out.println(" [x] Sent '" + message + " with routing: " + routingKey + "'");
        }

    }
  }
}