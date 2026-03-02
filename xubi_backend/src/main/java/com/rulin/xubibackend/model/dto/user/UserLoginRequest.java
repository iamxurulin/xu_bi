package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户登录请求类
 * 实现Serializable接口以支持序列化
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
public class UserLoginRequest implements Serializable {

    // 序列化版本UID，用于控制版本兼容性
    private static final long serialVersionUID = 3191241716373120793L;

    // 用户账号
    private String userAccount;

    // 用户密码
    private String userPassword;
}
