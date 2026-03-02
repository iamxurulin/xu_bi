package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新请求类
 * 用于封装用户更新请求的数据，实现了Serializable接口以支持序列化
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}