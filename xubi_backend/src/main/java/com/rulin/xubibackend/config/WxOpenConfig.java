package com.rulin.xubibackend.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信开放平台配置类
 * 用于配置和管理微信开放平台相关的参数和服务
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "wx.open")
@Data
public class WxOpenConfig {

    /**
     * 微信开放平台的应用ID
     */
    private String appId;

    /**
     * 微信开放平台的应用密钥
     */
    private String appSecret;

    /**
     * 微信公众号服务实例
     */
    private WxMpService wxMpService;

    /**
     * 获取微信公众号服务实例
     * 使用双重检查锁定模式确保线程安全地初始化服务实例
     *
     * @return 微信公众号服务实例
     */
    public WxMpService getWxMpService() {
        if (wxMpService != null) {
            return wxMpService;
        }
        synchronized (this) {
            if (wxMpService != null) {
                return wxMpService;
            }
            // 创建并配置微信默认配置实现类
            WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
            config.setAppId(appId);
            config.setSecret(appSecret);
            // 创建微信服务实例并设置配置
            WxMpService service = new WxMpServiceImpl();
            service.setWxMpConfigStorage(config);
            // 将服务实例保存到类变量中并返回
            wxMpService = service;
            return wxMpService;
        }
    }
}