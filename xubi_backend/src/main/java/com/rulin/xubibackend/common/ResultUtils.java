package com.rulin.xubibackend.common;


/**
 * ResultUtils工具类
 * 用于创建统一的结果响应对象
 */
public class ResultUtils {

    /**
     * 成功响应方法
     * @param data 返回的数据
     * @param <T> 数据类型
     * @return BaseResponse<T> 成功响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 使用错误码创建错误响应
     * @param errorCode 错误码枚举
     * @return BaseResponse 错误响应对象
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }


    /**
     * 使用错误码和消息创建错误响应
     * @param code 错误码
     * @param message 错误消息
     * @return BaseResponse 错误响应对象
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 使用错误码枚举和自定义消息创建错误响应
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return BaseResponse 错误响应对象
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
