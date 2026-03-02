package com.rulin.xubibackend.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;


/**
 * 用户角色枚举类
 * 定义了系统中不同的用户角色及其对应的文本和值
 */
public enum UserRoleEnum {


    // 枚举实例，包含中文描述和对应的英文标识
    USER("用户", "user"),      // 普通用户角色
    ADMIN("管理员", "admin"),  // 管理员角色
    BAN("被封号", "ban");     // 封禁用户角色

    // 角色的中文描述文本
    private final String text;

    // 角色的英文标识值
    private final String value;

    /**
     * 枚举构造函数
     *
     * @param text  角色的中文描述
     * @param value 角色的英文标识值
     */
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    /**
     * 根据值获取对应的枚举实例
     *
     * @param value 枚举值的字符串表示
     * @return 匹配的枚举实例，如果没有匹配则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {  // 检查输入值是否为空
            return null;  // 如果为空，直接返回null
        }
        // 遍历所有枚举实例
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            // 检查当前枚举实例的值是否与输入值匹配
            if (anEnum.value.equals(value)) {
                return anEnum;  // 如果匹配，返回该枚举实例
            }
        }
        return null;  // 如果没有匹配的枚举实例，返回null
    }

    /**
     * 获取枚举实例的值
     *
     * @return 枚举值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举实例的文本描述
     *
     * @return 枚举文本描述
     */
    public String getText() {
        return text;
    }
}
