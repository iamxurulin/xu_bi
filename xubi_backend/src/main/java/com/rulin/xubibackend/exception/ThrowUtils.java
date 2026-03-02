package com.rulin.xubibackend.exception;

import com.rulin.xubibackend.common.ErrorCode;

/**
 * ThrowUtils 工具类，提供条件抛出异常的便捷方法
 */
public class ThrowUtils {
    /**
     * 如果条件为真，则抛出指定的运行时异常
     * @param condition 判断条件
     * @param runtimeException 要抛出的运行时异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 如果条件为真，则抛出业务异常
     * @param condition 判断条件
     * @param errorCode 错误码，用于构造业务异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 如果条件为真，则抛出带自定义消息的业务异常
     * @param condition 判断条件
     * @param errorCode 错误码，用于构造业务异常
     * @param message 自定义异常消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
