package com.rulin.xubibackend.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;


/**
 * 登录用户视图对象，用于封装登录用户的相关信息
 * 该类实现了Serializable接口，支持序列化操作
 * 使用了@Data注解，可能来自Lombok库，自动生成getter、setter等方法
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
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
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}