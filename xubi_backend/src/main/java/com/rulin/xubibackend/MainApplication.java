package com.rulin.xubibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 * 该类是整个应用程序的入口点，负责启动Spring Boot应用
 *
 * @SpringBootApplication 注解标记该类为Spring Boot应用的启动类
 * exclude = {RedisAutoConfiguration.class} 表示默认不启用Redis自动配置
 * @MapperScan("com.rulin.xubibackend.mapper") 扫描指定包下的MyBatis Mapper接口
 * @EnableScheduling 启用Spring计划任务功能，允许使用@Scheduled注解
 * @EnableAspectJAutoProxy 启用AspectJ自动代理，支持AOP功能
 * proxyTargetClass = true 使用CGLIB代理而不是JDK动态代理
 * exposeProxy = true 暴露代理对象，使得目标对象可以通过AContext.currentProxy()获取
 */
// todo 如需开启 Redis，须移除 exclude 中的内容
// 提示：如果需要启用Redis功能，需要从exclude属性中移除RedisAutoConfiguration.class
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.rulin.xubibackend.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    /**
     * 程序主方法，应用程序的入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用程序
        SpringApplication.run(MainApplication.class, args);
    }

}
