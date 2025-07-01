package com.cpi.cpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.cpi.cpicturebackend.mapper")//mybatis-plus扫描
@EnableAspectJAutoProxy(exposeProxy = true)//Spring 会将当前的代理对象暴露到线程上下文（AopContext)中
public class CPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CPictureBackendApplication.class, args);
    }

}
