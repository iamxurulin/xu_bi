package com.rulin.xubibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @SpringBootApplication 注解标记该类为Spring Boot应用的启动类
 * @MapperScan("com.rulin.xubibackend.mapper") 扫描指定包下的MyBatis Mapper接口
 * @EnableScheduling 启用Spring计划任务功能，允许使用@Scheduled注解
 * @EnableAspectJAutoProxy 启用AspectJ自动代理，支持AOP功能
 * @EnableCaching 启用Spring Cache，支持@Cacheable/@CacheEvict等注解
 */
@SpringBootApplication
@MapperScan("com.rulin.xubibackend.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableCaching
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
