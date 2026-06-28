package com.rulin.xubibackend.wxmp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agnes AI 配置
 */
@Configuration
@ConfigurationProperties(prefix = "agnes.ai")
@Data
public class AgnesAiConfig {
    private String apiKey;
    private String baseUrl;
    private String modelName;
}
