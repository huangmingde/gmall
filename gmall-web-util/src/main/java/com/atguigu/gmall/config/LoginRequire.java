package com.atguigu.gmall.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)     //表示可以加在方法上
@Retention(RetentionPolicy.RUNTIME)     //生命周期为runtime。无论哪个时期都有效
public @interface LoginRequire {
    // true ：则表示需要登录，否则不需要登录！
    boolean autoRedirect() default true;
}
