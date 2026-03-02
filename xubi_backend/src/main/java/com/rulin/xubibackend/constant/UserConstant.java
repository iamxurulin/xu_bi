package com.rulin.xubibackend.constant;

/**
 * 用户相关的常量接口
 * 该接口定义了用户模块中使用的常量值
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    // 这是一个区域标记，用于标识权限相关的常量定义区域
    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user"; // 普通用户的默认角色标识

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin"; // 系统管理员的角色标识

    /**
     * 被封号
     */
    String BAN_ROLE = "ban"; // 被系统封禁的用户角色标识

    // endregion
    // 标记权限区域结束
}
