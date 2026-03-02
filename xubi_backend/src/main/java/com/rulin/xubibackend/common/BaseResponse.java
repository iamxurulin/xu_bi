package com.rulin.xubibackend.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 通用基础响应类，用于封装API返回结果
 * @param <T> 泛型类型，表示返回数据的类型
 */
@Data
public class BaseResponse<T> implements Serializable {

    // 状态码，表示API调用的结果状态
    private int code;

    // 响应数据，泛型类型，可以是任意类型的数据
    private T data;

    // 响应消息，对API调用结果的描述信息
    private String message;

    /**
     * 全参构造方法
     * @param code 状态码
     * @param data 响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 带状态码和数据的构造方法，消息默认为空字符串
     * @param code 状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 基于错误码的构造方法
     * @param errorCode 错误码枚举，包含状态码和错误信息
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
