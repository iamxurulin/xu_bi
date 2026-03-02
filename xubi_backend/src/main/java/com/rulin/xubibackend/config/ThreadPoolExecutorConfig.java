package com.rulin.xubibackend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 * 用于配置和创建一个自定义的线程池实例
 */
@Configuration
public class ThreadPoolExecutorConfig {

    /**
     * 创建并配置一个线程池的Bean
     *
     * @return 配置好的ThreadPoolExecutor实例
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //创建一个线程工厂，用于自定义线程的创建方式
        ThreadFactory threadFactory = new ThreadFactory() {
            //初始化线程数为1
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                //创建一个新的线程
                Thread thread = new Thread(r);

                //给新线程设置一个名称，名称中包含线程数的当前值
                thread.setName("线程" + count);

                //线程数递增
                count++;

                //返回新创建的线程
                return thread;
            }
        };
        /**
         * 创建一个线程池执行器
         * 参数说明：
         * 核心线程数：2
         * 最大线程数：4
         * 空闲线程存活时间：100秒
         * 时间单位：秒
         * 工作队列：容量为4的ArrayBlockingQueue
         * 线程工厂：threadFactory
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory);
        //返回创建的线程池
        return threadPoolExecutor;
    }
}
