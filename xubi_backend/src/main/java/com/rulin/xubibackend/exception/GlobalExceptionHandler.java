package com.rulin.xubibackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rulin.xubibackend.common.BaseResponse;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.common.ResultUtils;


/**
 * 全局异常处理器
 * 使用@RestControllerAdvice注解实现全局异常处理
 * 使用@Slf4j注解实现日志记录功能
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常对象
     * @return 返回错误信息的BaseResponse对象
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     * @param e 运行时异常对象
     * @return 返回系统错误信息的BaseResponse对象
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
