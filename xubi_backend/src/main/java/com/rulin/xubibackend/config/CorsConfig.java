package com.rulin.xubibackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * CORS跨域配置类
 * 实现WebMvcConfigurer接口，配置跨域资源共享策略
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域映射规则
     * @param registry CORS注册对象，用于添加跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")            // 设置跨域映射路径，"/**"表示匹配所有路径
                // 允许发送 Cookie
                .allowCredentials(true)      // 允许跨域请求携带认证信息（如Cookie）
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")  // 设置允许跨域请求的源模式，"*"表示所有域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 设置允许的HTTP方法
                .allowedHeaders("*")          // 设置允许的请求头，"*"表示所有请求头
                .exposedHeaders("*");        // 设置响应头中允许访问的头，"*"表示所有响应头
    }
}
