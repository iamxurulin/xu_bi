package com.rulin.xubibackend.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类，用于获取Spring容器中的Bean
 * 实现了ApplicationContextAware接口，以便获取Spring上下文
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * Spring上下文对象，静态变量，以便在整个应用程序中共享
     */
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的方法
     * 将Spring上下文注入到静态变量中
     *
     * @param applicationContext Spring上下文对象
     * @throws BeansException 如果获取上下文失败则抛出此异常
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 通过名称获取 Bean
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 通过名称和类型获取 Bean
     *
     * @param beanName
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return applicationContext.getBean(beanName, beanClass);
    }
}