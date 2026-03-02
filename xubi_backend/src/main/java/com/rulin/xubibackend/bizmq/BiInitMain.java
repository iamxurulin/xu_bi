package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * BiInitMain类
 * 用于初始化BI相关的消息队列连接和配置
 */
public class BiInitMain {
    /**
     * 主方法，用于建立与消息队列的连接并设置相关配置
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            // 设置连接主机地址为本地
            factory.setHost("localhost");

            // 创建新的连接
            Connection connection = factory.newConnection();

            // 创建通道
            Channel channel = connection.createChannel();

            // 定义交换机名称，使用常量BI_EXCHANGE_NAME
            String EXCHANGE_NAME = BiMqConstant.BI_EXCHANGE_NAME;

            // 声明一个direct类型的交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 定义队列名称，使用常量BI_QUEUE_NAME
            String queueName = BiMqConstant.BI_QUEUE_NAME;

            // 声明一个持久化的队列
            channel.queueDeclare(queueName, true, false, false, null);

            // 将队列与交换机通过指定的路由键绑定
            channel.queueBind(queueName, EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);

        } catch (Exception e) {

            // 异常处理（空实现，可根据需要添加具体的异常处理逻辑）
        }
    }
}
