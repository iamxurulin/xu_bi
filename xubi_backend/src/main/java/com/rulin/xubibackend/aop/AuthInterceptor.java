package com.rulin.xubibackend.aop;

import com.rulin.xubibackend.model.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.rulin.xubibackend.annotation.AuthCheck;
import com.rulin.xubibackend.common.ErrorCode;
import com.rulin.xubibackend.exception.BusinessException;
import com.rulin.xubibackend.model.enums.UserRoleEnum;
import com.rulin.xubibackend.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限拦截器，用于处理带有@AuthCheck注解的方法的权限验证
 * 使用AOP(面向切面编程)实现，在方法执行前进行权限检查
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 使用@Resource注解注入UserService接口的实现类
     * @Resource是JSR-250规范提供的注解，默认按名称进行注入
     * 当名称无法匹配时，会按类型进行注入
     * 这里将注入UserService接口的具体实现类到userService私有字段中
     */
    @Resource
    private UserService userService;

    /**
     * 环绕通知，在带有@AuthCheck注解的方法执行前后进行权限验证
     * @param joinPoint 连接点，可以获取目标方法的信息
     * @param authCheck 权限检查注解，包含需要的权限信息
     * @return 目标方法的执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取注解中指定的必须角色
        String mustRole = authCheck.mustRole();
        // 获取当前请求的属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 从请求属性中获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);  // 获取当前登录用户信息
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);  // 将需要的权限值转换为枚举类型
        // 不需要权限，放行
        if (mustRoleEnum == null) {  // 如果不需要特定权限（mustRole为null），则直接放行
            return joinPoint.proceed();
        }
        // 必须有该权限才通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());  // 获取当前用户的权限枚举类型
        if (userRoleEnum == null) {  // 如果用户没有有效权限，抛出无权限异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {  // 检查用户是否被封号，封号用户直接拒绝访问
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {  // 如果需要管理员权限
            // 用户没有管理员权限，拒绝
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {  // 检查当前用户是否具有管理员权限，没有则抛出异常
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();  // 所有权限验证通过，继续执行目标方法
    }
}

