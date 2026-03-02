package com.rulin.xubibackend.common;

/**
 * 错误码枚举类
 * 用于定义系统中可能出现的各种错误及其对应的错误码和错误信息
 */
public enum ErrorCode {

    // 成功状态码
    SUCCESS(0, "ok"),

    // 客户端错误码 - 4xx
    PARAMS_ERROR(40000, "请求参数错误"),      // 请求参数错误
    NOT_LOGIN_ERROR(40100, "未登录"),        // 未登录错误
    NO_AUTH_ERROR(40101, "无权限"),          // 无权限错误
    NOT_FOUND_ERROR(40400, "请求数据不存在"), // 请求数据不存在
    TOO_MANY_REQUEST(42900, "请求过于频繁"),  // 请求过于频繁
    FORBIDDEN_ERROR(40300, "禁止访问"),      // 禁止访问

    // 服务端错误码 - 5xx
    SYSTEM_ERROR(50000, "系统内部异常"),      // 系统内部异常
    OPERATION_ERROR(50001, "操作失败");      // 操作失败

    // 错误码，用于标识具体的错误类型
    private final int code;

    // 错误信息，用于向用户展示具体的错误描述
    private final String message;

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误信息
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * @return 返回错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误信息
     * @return 返回错误信息
     */
    public String getMessage() {
        return message;
    }

}
