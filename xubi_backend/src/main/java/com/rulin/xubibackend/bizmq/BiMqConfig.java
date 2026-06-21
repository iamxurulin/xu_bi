package com.rulin.xubibackend.bizmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.UUID;

/**
 * BI 消息队列配置类
 * 替代 CLI 脚本 BiInitMain，以 Spring Bean 方式管理 RabbitMQ 拓扑和可靠性
 */
@Configuration
@Slf4j
public class BiMqConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Resource
    private ConnectionFactory connectionFactory;

    // Exchange/Queue/Binding 不声明为 @Bean，避免 Spring Boot 自动声明导致 DLX 冲突
    // 所有拓扑由 mqAdmin 手动声明

    private DirectExchange biExchange;
    private DirectExchange biDlxExchange;
    private Queue biQueue;
    private Queue biDlqQueue;
    private Binding biBinding;
    private Binding biDlqBinding;
    private java.util.Map<String, Object> dlxArgs;

    /**
     * 自定义 RabbitAdmin Bean
     * 注意：该方法本身创建一个 RabbitAdmin，但 @PostConstruct 中的拓扑初始化逻辑不依赖此 bean（避免循环引用）。
     * 该 bean 供其他需要 RabbitAdmin 的组件使用。
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // ==================== 队列迁移 + 拓扑初始化 ====================

    /**
     * 启动时执行 bi_queue DLX 迁移，然后声明所有 RabbitMQ 拓扑
     * 直接使用 ConnectionFactory 创建临时 RabbitAdmin，避免与自身的 rabbitAdmin bean 循环引用
     */
    @PostConstruct
    public void initRabbitMqTopology() {
        try {
            RabbitAdmin mqAdmin = new RabbitAdmin(connectionFactory);
            mqAdmin.initialize();

            dlxArgs = new java.util.HashMap<>();
            dlxArgs.put("x-dead-letter-exchange", "bi_dlx_exchange");
            dlxArgs.put("x-dead-letter-routing-key", "bi_dlk");

            // 检查并迁移旧版 bi_queue
            java.util.Properties queueInfo = mqAdmin.getQueueProperties(BiMqConstant.BI_QUEUE_NAME);
            if (queueInfo != null) {
                Object argumentsObj = queueInfo.get("arguments");
                if (argumentsObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> arguments = (java.util.Map<String, Object>) argumentsObj;
                    if (!"bi_dlx_exchange".equals(arguments.get("x-dead-letter-exchange"))) {
                        log.warn("旧版 bi_queue 未配置 DLX，正在删除并重建...");
                        mqAdmin.deleteQueue(BiMqConstant.BI_QUEUE_NAME);
                        biQueue = new Queue(BiMqConstant.BI_QUEUE_NAME, true, false, false, dlxArgs);
                        mqAdmin.declareQueue(biQueue);
                        log.info("bi_queue 重建完成");
                    } else {
                        log.info("bi_queue 已配置 DLX，无需迁移");
                        biQueue = new Queue(BiMqConstant.BI_QUEUE_NAME, true, false, false, dlxArgs);
                    }
                } else {
                    log.warn("bi_queue arguments 格式异常，删除并重建");
                    mqAdmin.deleteQueue(BiMqConstant.BI_QUEUE_NAME);
                    biQueue = new Queue(BiMqConstant.BI_QUEUE_NAME, true, false, false, dlxArgs);
                    mqAdmin.declareQueue(biQueue);
                    log.info("bi_queue 重建完成");
                }
            } else {
                biQueue = new Queue(BiMqConstant.BI_QUEUE_NAME, true, false, false, dlxArgs);
                mqAdmin.declareQueue(biQueue);
                log.info("bi_queue 创建完成");
            }

            // 声明 DLQ
            biDlqQueue = new Queue("bi_dlq", true, false, false);
            mqAdmin.declareQueue(biDlqQueue);

            // 声明交换机
            biExchange = new DirectExchange(BiMqConstant.BI_EXCHANGE_NAME, true, false);
            mqAdmin.declareExchange(biExchange);

            biDlxExchange = new DirectExchange("bi_dlx_exchange", true, false);
            mqAdmin.declareExchange(biDlxExchange);

            // 声明绑定
            biBinding = BindingBuilder.bind(biQueue).to(biExchange).with(BiMqConstant.BI_ROUTING_KEY);
            mqAdmin.declareBinding(biBinding);

            biDlqBinding = BindingBuilder.bind(biDlqQueue).to(biDlxExchange).with("bi_dlk");
            mqAdmin.declareBinding(biDlqBinding);

            log.info("BI RabbitMQ 拓扑初始化完成");

        } catch (Exception e) {
            log.error("BI RabbitMQ 初始化失败: {}", e.getMessage(), e);
        }
    }

    // ==================== RabbitTemplate（ConfirmCallback + ReturnCallback） ====================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 消息路由不到队列时触发 ReturnCallback
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("RabbitMQ ReturnCallback: message={},"
                            + " replyCode={}, replyText={}, exchange={}, routingKey={}",
                    message.getMessageProperties().getMessageId(),
                    replyCode, replyText, exchange, routingKey);
        });
        // 消息到达 Exchange 后触发 ConfirmCallback
        rabbitTemplate.setConfirmCallback((correlationData, ack, reason) -> {
            if (ack) {
                log.info("RabbitMQ ConfirmCallback: message {} confirmed",
                        correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.warn("RabbitMQ ConfirmCallback: message {} failed, reason: {}",
                        correlationData != null ? correlationData.getId() : "unknown", reason);
            }
        });
        return rabbitTemplate;
    }

    // ==================== 消息 ID 后置处理器 ====================

    /**
     * 为每条 BI 消息自动添加 x-message-id (UUID) 头，用于消费者幂等
     */
    public static MessagePostProcessor messageIdProcessor() {
        return new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) {
                String messageId = UUID.randomUUID().toString().replace("-", "");
                message.getMessageProperties().setMessageId(messageId);
                return message;
            }
        };
    }

    // ==================== 重试机制 ====================

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 最多重试 3 次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 每次重试间隔 1 秒
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 重试日志
        BiRetryListener retryListener = new BiRetryListener();
        retryTemplate.setListeners(new org.springframework.retry.RetryListener[]{retryListener});

        return retryTemplate;
    }

    // ==================== ContainerFactory ====================

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryTemplate retryTemplate) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        factory.setRetryTemplate(retryTemplate);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory dlqListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        // 死信队列不需要重试，消费后直接 ack
        return factory;
    }
}
