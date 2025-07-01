package com.cpi.cpicturebackend.aop;


import com.cpi.cpicturebackend.annotation.AuthCheck;
import com.cpi.cpicturebackend.exception.BusinessException;
import com.cpi.cpicturebackend.exception.ErrorCode;
import com.cpi.cpicturebackend.model.entity.User;
import com.cpi.cpicturebackend.model.enums.UserRoleEnum;
import com.cpi.cpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
// 环绕通知，用于在方法执行前后进行拦截处理
@Around("@annotation(authCheck)")
public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
    // 获取方法上AuthCheck注解指定的角色
    String mustRole = authCheck.mustRole();
    // 获取当前请求的属性
    RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    // 当前登录用户
    User loginUser = userService.getLoginUser(request);
    // 根据方法注解中的角色值获取对应的用户角色枚举
    UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
    // 不需要权限，放行
    if (mustRoleEnum == null) {
        return joinPoint.proceed();
    }
    // 以下为：必须有该权限才通过
    // 获取当前用户具有的权限
    UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
    // 没有权限，拒绝
    if (userRoleEnum == null) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    // 要求必须有管理员权限，但用户没有管理员权限，拒绝
    if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
    // 通过权限校验，放行
    return joinPoint.proceed();
}
}
