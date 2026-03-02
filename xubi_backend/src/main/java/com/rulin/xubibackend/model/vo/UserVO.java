package com.rulin.xubibackend.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;


/**
 * 用户值对象（Value Object）类，用于封装用户相关的数据信息
 * 实现了Serializable接口，支持序列化操作
 */
@Data  // 使用Lombok注解自动生成getter、setter、toString等方法
public class UserVO implements Serializable {

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

    private static final long serialVersionUID = 1L;
}