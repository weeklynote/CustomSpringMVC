package com.mar.annotation;

import java.lang.annotation.*;

/**
 * @Author: 刘劲
 * @Date: 2020/4/21 12:16
 * 如果同时作用在类和方法上，那么作用在方法上的权限会覆盖作用在类上的权限
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MySecurity {
    String[] value() default {};
}
