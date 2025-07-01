package com.cpi.cpicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//注解生效范围
@Retention(RetentionPolicy.RUNTIME)//注解在什么时候生效
public @interface AuthCheck {

    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}
