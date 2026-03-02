package com.rulin.xubibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * RabbitMQ初始化主类
 * 用于创建连接、通道、交换器和队列，并进行绑定
 */
public class MqInitMain {
    public static void main(String[] args){
        try{
            //创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置RabbitMQ服务器主机地址
            factory.setHost("localhost");

            //创建连接
            Connection connection = factory.newConnection();

            //创建通道
            Channel channel = connection.createChannel();

            //定义交换器名称
            String EXCHANGE_NAME = "code_exchange";

            //声明交换器，类型为direct
            channel.exchangeDeclare(EXCHANGE_NAME,"direct");

            //定义队列名称
            String queueName = "code_queue";

            //声明队列，设置持久化、非独占、非自动删除
            channel.queueDeclare(queueName,true,false,false,null);

            //将队列与交换器绑定，指定路由键
            channel.queueBind(queueName,EXCHANGE_NAME,"my_routingKey");

        }catch (Exception e){

            //异常处理（当前为空实现）
        }
    }
}
