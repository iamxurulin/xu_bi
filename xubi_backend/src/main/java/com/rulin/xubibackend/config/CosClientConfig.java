package com.rulin.xubibackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云对象存储(COS)客户端配置类
 * 使用@Configuration注解表明这是一个配置类
 * 使用@ConfigurationProperties注解将配置文件中以"cos.client"为前缀的属性注入到此类中
 * 使用@Data注解为类自动生成getter、setter等方法
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    // 腾讯云访问密钥ID
    private String accessKey;

    // 腾讯云访问密钥Secret
    private String secretKey;

    // COS地域信息
    private String region;

    // 存储桶名称
    private String bucket;

    /**
     * 创建并配置COSClient Bean
     * @return 配置好的COSClient实例
     */
    @Bean
    public COSClient cosClient() {

        // 初始化腾讯云凭证
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 初始化客户端配置，设置地域信息
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 返回配置好的COSClient实例
        return new COSClient(cred, clientConfig);
    }
}