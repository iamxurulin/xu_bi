package com.rulin.xubibackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 用于配置和创建Redisson客户端实例
 */
@Configuration // 标识为配置类
@ConfigurationProperties(prefix = "spring.redis") // 绑定配置文件中以spring.redis为前缀的属性
@Data // 使用Lombok自动生成getter、setter等方法
public class RedissonConfig {

    private Integer database; // Redis数据库索引

    private String host; // Redis服务器主机

    private Integer port; // Redis服务器端口

    /**
     * 创建并配置Redisson客户端
     * @return 返回配置好的Redisson客户端实例
     */
    @Bean // 将该方法返回的对象注入到Spring容器中
    public RedissonClient getRedissonClient() {
        //1.创建配置对象
        Config config = new Config();
        // 配置单服务器模式，设置数据库索引和服务器地址
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port);
        //2.创建Redisson实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
