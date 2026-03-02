package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;


@Data // 使用Lombok注解自动生成getter、setter、toString等方法
public class UserAddRequest implements Serializable { // 实现Serializable接口以支持序列化

    /**
     * 用户昵称
     * 用于显示的用户名称
     */
    private String userName;

    /**
     * 账号
     * 用户的登录账号
     */
    private String userAccount;

    /**
     * 用户头像
     * 存储头像图片的URL或路径
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin
     * 定义用户在系统中的角色和权限
     * user - 普通用户
     * admin - 管理员
     */
    private String userRole;

    private static final long serialVersionUID = 1L; // 序列化版本UID，用于版本控制
}