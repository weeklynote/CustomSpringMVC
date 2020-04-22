package com.mar.service.impl;

import com.mar.annotation.MvcService;
import com.mar.service.DemoService;

/**
 * @Author: 刘劲
 * @Date: 2020/4/19 10:23
 */
@MvcService
public class DemoServiceImpl implements DemoService {

    @Override
    public String get(String name) {
        System.err.println("Service 实现类中的name:" + name);
        return name;
    }
}
