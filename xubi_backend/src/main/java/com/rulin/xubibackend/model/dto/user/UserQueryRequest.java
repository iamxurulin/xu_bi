package com.rulin.xubibackend.model.dto.user;

import java.io.Serializable;

import com.rulin.xubibackend.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

// 使用Lombok注解，自动生成equals和hashCode方法，并调用父类的equals和hashCode方法
@EqualsAndHashCode(callSuper = true)
// 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
@Data
/**
 * UserQueryRequest类，用于用户查询请求的参数封装
 * 继承自PageRequest类，实现Serializable接口，支持序列化
 */
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 开放平台id
     */
    private String unionId;

    /**
     * 公众号openId
     */
    private String mpOpenId;

    /**
     * 用户昵称
     */
    private String userName;

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