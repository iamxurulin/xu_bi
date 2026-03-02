package com.rulin.xubibackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatis-Plus配置类
 * 用于配置MyBatis-Plus的相关功能，如分页插件等
 */
@Configuration // 标记该类为配置类，Spring会扫描并加载其中的配置
@MapperScan("com.rulin.xubibackend.mapper") // 扫描指定包下的Mapper接口，自动生成代理对象
public class MyBatisPlusConfig {


    /**
     * 配置MyBatis-Plus的插件
     * @return MybatisPlusInterceptor 配置了分页插件的拦截器
     */
    @Bean // 将方法的返回值交给Spring容器管理，作为Bean组件
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建MyBatis-Plus拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，并指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor; // 返回配置好的拦截器
    }
}