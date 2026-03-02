package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求类
 * 该类实现了Serializable接口，用于支持序列化操作
 * 使用@Data注解来自动生成getter、setter、toString等方法
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     * 用于存储和显示用户的昵称信息
     */
    private String userName;

    /**
     * 用户头像
     * 存储用户头像的URL或标识信息
     */
    private String userAvatar;

    /**
     * 简介
     * 存储用户的个人简介或描述信息
     */
    private String userProfile;

    // 序列化版本UID，用于实现Serializable接口
    private static final long serialVersionUID = 1L;
}