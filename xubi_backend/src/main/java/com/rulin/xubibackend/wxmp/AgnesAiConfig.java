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
    private String apiKey = "REMOVED_API_KEY";
    private String baseUrl = "https://apihub.agnes-ai.com/v1";
    private String modelName = "agnes-2.0-flash";
}
