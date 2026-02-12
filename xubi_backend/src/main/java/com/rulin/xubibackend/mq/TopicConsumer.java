package com.rulin.xubibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        String queueName = "frontend_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "#.前端.#");

        String queueName2 = "backend_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");

        String queueName3 = "product_queue";
        channel.queueDeclare(queueName3, true, false, false, null);
        channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback fengdeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [feng] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback lindeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [lin] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        DeliverCallback huodeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [huo] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, fengdeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, lindeliverCallback, consumerTag -> {
        });
        channel.basicConsume(queueName3, true, huodeliverCallback, consumerTag -> {
        });

    }
}