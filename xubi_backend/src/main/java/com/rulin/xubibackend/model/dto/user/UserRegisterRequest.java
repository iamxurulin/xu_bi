package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户注册请求类，实现Serializable接口以支持序列化
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class UserRegisterRequest implements Serializable {

    // 序列化版本UID，用于版本控制
    private static final long serialVersionUID = 3191241716373120793L;

    // 用户账号
    private String userAccount;

    // 用户密码
    private String userPassword;

    // 确认密码
    private String checkPassword;
}
