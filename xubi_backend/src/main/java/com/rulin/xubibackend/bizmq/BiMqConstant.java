package com.rulin.xubibackend.bizmq;

/**
 * 定义与BI消息队列相关的常量接口
 * 该接口包含了BI系统中消息队列所需的基本名称常量
 */
public interface BiMqConstant {

    // BI系统交换机名称，用于消息的路由分发
    String BI_EXCHANGE_NAME = "bi_exchange";
    // BI系统队列名称，用于消息的存储和消费
    String BI_QUEUE_NAME = "bi_queue";
    // BI系统路由键名称，用于消息的路由规则匹配
    String BI_ROUTING_KEY = "bi_routingKey";
}
