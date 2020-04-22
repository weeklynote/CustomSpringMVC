package com.mar.annotation;

import java.lang.annotation.*;

/**
 * @Author: 刘劲
 * @Date: 2020/4/18 23:45
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MvcService {

    String value() default "";
}
