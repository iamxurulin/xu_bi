package com.rulin.xubibackend.exception;

import com.rulin.xubibackend.common.ErrorCode;


/**
 * 自定义业务异常类，继承自RuntimeException
 * 用于在业务逻辑处理过程中抛出特定的异常情况
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码，用于标识具体的错误类型
     * 使用final修饰，确保在对象创建后不可修改
     */
    private final int code;

    /**
     * 构造方法1：根据错误码和自定义错误信息创建异常对象
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法2：根据预定义的错误枚举创建异常对象
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法3：根据预定义的错误枚举和自定义错误信息创建异常对象
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
