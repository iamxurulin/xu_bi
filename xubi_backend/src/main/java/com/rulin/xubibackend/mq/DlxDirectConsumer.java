package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxDirectConsumer {

    private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";
    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        Map<String,Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key","waibao");

        String queueName = "dog_queue";
        channel.queueDeclare(queueName, true, false, false, args);
        channel.queueBind(queueName, WORK_EXCHANGE_NAME, "dog");

        Map<String,Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key","boss");

        String queueName2 = "cat_queue";
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "cat");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback ruleideliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [dog] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback ruyindeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [cat] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, false, ruleideliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, ruyindeliverCallback, consumerTag -> {
        });

    }
}