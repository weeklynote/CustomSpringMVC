package com.mar.interceptor;

import com.mar.servlet.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: 刘劲
 * @Date: 2020/4/21 12:19
 */
public interface Interceptor {

    boolean intercept(HttpServletRequest req, HttpServletResponse resp, Handler handler);
}
