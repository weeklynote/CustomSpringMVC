package com.mar.controller;

import com.mar.annotation.MvcAutoWired;
import com.mar.annotation.MvcController;
import com.mar.annotation.MvcRequestMapping;
import com.mar.annotation.MySecurity;
import com.mar.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: 刘劲
 * @Date: 2020/4/19 10:22
 */
@MvcController
@MvcRequestMapping("/test")
@MySecurity(value = {"lisi", "zhangsan"})
public class MyController {

    @MvcAutoWired
    private DemoService demoService;

    @MvcRequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response, String username){
        final String s = demoService.get(username);
        System.err.println("MyController query:" + username);
        return "";
    }

    @MySecurity(value = {"lisi"})
    @MvcRequestMapping("/queryV2")
    public String queryV2(HttpServletRequest request, HttpServletResponse response, String username){
        final String s = demoService.get(username);
        System.err.println("MyController queryV2:" + username);
        return s;
    }

    @MySecurity(value = {"zhangsan"})
    @MvcRequestMapping("/queryV3")
    public String queryV3(HttpServletRequest request, HttpServletResponse response, String username){
        final String s = demoService.get(username);
        System.err.println("MyController queryV2:" + username);
        return s;
    }

    @MySecurity(value = {"wangwu"})
    @MvcRequestMapping("/queryV4")
    public String queryV4(HttpServletRequest request, HttpServletResponse response, String username){
        final String s = demoService.get(username);
        System.err.println("MyController queryV2:" + username);
        return s;
    }
}
