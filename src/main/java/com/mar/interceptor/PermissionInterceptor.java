package com.mar.interceptor;

import com.mar.servlet.Handler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: 刘劲
 * @Date: 2020/4/21 12:19
 */
public class PermissionInterceptor implements Interceptor {

    private final static String NAME = "username";

    @Override
    public boolean intercept(HttpServletRequest req, HttpServletResponse resp, Handler handler) {
        final String[] securities = handler.getSecurities();
        final String name = req.getParameter(NAME);
        if (securities != null && securities.length > 0){
            for (String security : securities) {
                if (StringUtils.equals(security, name)){
                    return false;
                }
            }
        }else {
            return false;
        }
        try {
            // 处理中文乱码
            resp.setHeader("Content-type", "text/html;charset=UTF-8");
            resp.getWriter().write("Permission Denied!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
