package com.rulin.xubibackend.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;


/**
 * 文件上传业务类型枚举类
 * 用于定义系统中不同类型的文件上传业务场景
 */
public enum FileUploadBizEnum {

    // 用户头像业务类型
    USER_AVATAR("用户头像", "user_avatar");

    // 枚举的文本描述，用于界面展示
    private final String text;

    // 枚举的值，用于业务逻辑处理
    private final String value;

    /**
     * 枚举构造函数
     * @param text 枚举的文本描述
     * @param value 枚举的值
     */
    FileUploadBizEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 获取所有枚举值的集合
     * @return 包含所有枚举值的List集合
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据枚举值获取对应的枚举对象
     * @param value 枚举值
     * @return 匹配的枚举对象，如果未找到则返回null
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 获取枚举值
     * @return 枚举的值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举的文本描述
     * @return 枚举的文本描述
     */
    public String getText() {
        return text;
    }
}
