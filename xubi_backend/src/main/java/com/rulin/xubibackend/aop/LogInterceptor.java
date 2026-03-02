package com.rulin.xubibackend.aop;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * LogInterceptor类是一个切面类，用于拦截和处理Controller层的请求日志记录。
 * 该类使用Spring AOP功能，实现了请求前后的日志记录，包括请求参数、响应时间和请求ID等信息。
 */
@Aspect // 声明这是一个切面类，用于实现AOP功能
@Component // 将此类声明为Spring组件，使其被Spring容器管理
@Slf4j // 使用@Slf4j注解自动生成日志器，简化日志记录代码
public class LogInterceptor {
    /**
     * 环绕通知，拦截controller包下的所有方法
     * 在方法执行前后进行日志记录，并计算方法执行时间
     * @param point 连接点，可以获取被拦截方法的信息和参数
     * @return 方法执行结果
     * @throws Throwable 方法执行可能抛出的异常
     */
    @Around("execution(* com.rulin.xubibackend.controller.*.*(..))") // 定义环绕通知，拦截controller包下的所有方法
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时：创建一个StopWatch对象用于计算方法执行时间
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 获取请求路径：获取当前请求的RequestAttributes，进而获取HttpServletRequest对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 生成请求唯一 id：使用UUID生成请求的唯一标识
        String requestId = UUID.randomUUID().toString();
        String uri = httpServletRequest.getRequestURI(); // 获取请求的URI
        // 获取请求参数：获取方法的参数数组，并将其转换为字符串
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        // 输出请求日志：记录请求开始的相关信息，包括请求ID、路径、IP和参数
        log.info("request start, id: {}, path: {}, ip: {}, params: {}", requestId, uri,
                httpServletRequest.getRemoteHost(), reqParam);
        // 执行原方法：调用被拦截的方法，并获取返回结果
        Object result = point.proceed();
        // 输出响应日志：记录请求结束的信息，包括请求ID和总耗时
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);
        return result; // 返回方法执行结果
    }
}

