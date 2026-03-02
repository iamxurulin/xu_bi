package com.rulin.xubibackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 * 这是一个自定义注解，用于标记需要进行权限校验的方法
 */
@Target(ElementType.METHOD)  // 表明该注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 表明该注解会在运行时保留，可以通过反射获取
public @interface AuthCheck {  // 定义一个名为AuthCheck的注解

    /**
     * 必须有某个角色
     * 指定用户必须具备的角色才能访问被注解标记的方法
     * @return 返回角色名称字符串，默认值为空字符串
     */
    String mustRole() default "";  // 定义一个名为mustRole的属性，类型为String，默认值为空字符串

}

