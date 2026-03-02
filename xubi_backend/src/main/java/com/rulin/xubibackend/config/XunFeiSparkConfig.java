package com.rulin.xubibackend.config;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 讯飞Spark配置类
 * 用于配置和管理讯飞Spark客户端的相关属性
 */
@Configuration
@ConfigurationProperties(prefix = "xun-fei.client")
@Data
public class XunFeiSparkConfig {
    // 讯飞Spark应用ID
    private String appId;
    // 讯飞Spark API密钥
    private String apiSecret;
    // 讯飞Spark API-Key
    private String apiKey;

    /**
     * 创建并配置SparkClient Bean
     * @return 配置好的SparkClient实例
     */
    @Bean
    public SparkClient sparkClient(){
        SparkClient sparkClient = new SparkClient();
        // 设置API-Key
        sparkClient.apiKey = apiKey;
        // 设置API密钥
        sparkClient.apiSecret = apiSecret;
        // 设置应用ID
        sparkClient.appid = appId;
        return sparkClient;
    }
}
