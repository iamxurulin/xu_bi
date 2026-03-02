package com.rulin.xubibackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


/**
 * JsonConfig类是一个用于配置Jackson JSON序列化器的组件
 * 通过@JsonComponent注解标记为Spring的JSON组件，使其自动注册到Spring上下文中
 */
@JsonComponent
public class JsonConfig {
    /**
     * 配置并返回一个自定义的ObjectMapper实例
     * 该配置主要解决Long类型数据在JSON序列化时可能出现的精度问题
     *
     * @param builder Jackson2ObjectMapperBuilder，Spring提供的构建器
     * @return 配置好的ObjectMapper实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 创建ObjectMapper实例，禁用XML映射功能
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // 创建一个新的简单模块，用于添加自定义序列化器
        SimpleModule module = new SimpleModule();
        // 添加Long类型的序列化器，将其转换为字符串形式，避免精度丢失
        module.addSerializer(Long.class, ToStringSerializer.instance);
        // 添加long基本类型的序列化器，同样转换为字符串形式
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 注册自定义模块到ObjectMapper中
        objectMapper.registerModule(module);
        // 返回配置完成的ObjectMapper实例
        return objectMapper;
    }
}