package com.mar.servlet;

import com.mar.annotation.*;
import com.mar.interceptor.Interceptor;
import com.mar.interceptor.PermissionInterceptor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: 刘劲
 * @Date: 2020/4/18 23:43
 */
public class SpringMvcServlet extends HttpServlet {

    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>(16);
    private Map<String, Object> iocMapping = new HashMap<>(16);
    private List<Handler> handlerMapping = new ArrayList<>(16);
    private static final List<Interceptor> interceptors = new ArrayList<>(16);
    static {
        interceptors.add(new PermissionInterceptor());
    }

    @Override
    public void init(ServletConfig config) {
        final String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        // 加载配置信息
        doLoadConfig(contextConfigLocation);
        String pakName = properties.getProperty("scanpackage");
        // 扫描相关类
        doScan(pakName);
        // 初始化Bean对象
        try {
            doInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        // 实现依赖注入
        doAutoWired();
        // 构造HandlerMapping处理器，配置好url和Method的映射关系
        initHandlerMapping();
        System.err.println("初始化完成！！！！！");
    }

    private void initHandlerMapping(){
        if (iocMapping.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : iocMapping.entrySet()) {
            final Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(MvcController.class)){
                continue;
            }
            String baseUrl = "";
            if (aClass.isAnnotationPresent(MvcRequestMapping.class)){
                final MvcRequestMapping annotation = aClass.getAnnotation(MvcRequestMapping.class);
                baseUrl = annotation.value();
            }
            boolean controllerHasSecurity = aClass.isAnnotationPresent(MySecurity.class);
            String[] securitys = null;
            if (controllerHasSecurity){
                final MySecurity annotation = aClass.getAnnotation(MySecurity.class);
                securitys = annotation.value();
            }
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(MvcRequestMapping.class)){
                    final MvcRequestMapping annotation = method.getAnnotation(MvcRequestMapping.class);
                    String value = annotation.value();
                    String url = baseUrl + value;
                    Handler handler = new Handler(entry.getValue(), method, Pattern.compile(url));
                    // 计算参数位置信息
                    final Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        Parameter parameter = parameters[i];
                        final Class<?> type = parameter.getType();
                        if (type == HttpServletResponse.class || type == HttpServletRequest.class){
                            handler.getParamIndexMapping().put(parameter.getType().getName(), i);
                        }else {
                            handler.getParamIndexMapping().put(parameter.getName(), i);
                        }
                    }
                    if (method.isAnnotationPresent(MySecurity.class)) {
                        final MySecurity security = method.getAnnotation(MySecurity.class);
                        final String[] methodSecuritys = security.value();
                        if(methodSecuritys != null && methodSecuritys.length > 0){
                            handler.setSecurities(methodSecuritys);
                        }else {
                            handler.setSecurities(securitys);
                        }
                    }
                    handlerMapping.add(handler);
                }
            }
        }
    }

    private void doAutoWired(){
        if (iocMapping.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : iocMapping.entrySet()) {
            final Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(MvcAutoWired.class)){
                    final MvcAutoWired annotation = field.getAnnotation(MvcAutoWired.class);
                    String beanName = annotation.value();
                    if (beanName == null || "".equals(beanName.trim())){
                        beanName = field.getType().getName();
                    }
                    final Object o = iocMapping.get(beanName);
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(), o);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void doInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classNames.size() == 0){
            return;
        }
        for (String className : classNames) {
            Class<?> claz = Class.forName(className);
            // 区分Controller和Service
            if (claz.isAnnotationPresent(MvcController.class)){
                final String id = claz.getName();
                Object obj = claz.newInstance();
                iocMapping.put(id, obj);
            }else if (claz.isAnnotationPresent(MvcService.class)){
                final MvcService annotation = claz.getAnnotation(MvcService.class);
                String beanName = annotation.value();
                if (beanName == null || "".equals(beanName.trim()) ){
                    beanName = claz.getName();
                }
                Object obj = claz.newInstance();
                iocMapping.put(beanName, obj);
                final Class<?>[] interfaces = claz.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    iocMapping.put(anInterface.getName(), obj);
                }

            }else {
                continue;
            }
        }
    }

    private void doScan(String packageName){
        String scanPackage = Thread.currentThread().getContextClassLoader().getResource("").getPath() + packageName.replaceAll("\\.", "/");
        File pack = new File(scanPackage);
        final File[] files = pack.listFiles();
        for (File file : files) {
            if (file.isDirectory()){
                doScan(packageName + "." + file.getName());
            }else if (file.getName().endsWith(".class")){
                String clazName = packageName + "." + file.getName().replaceAll(".class", "");
                classNames.add(clazName);
            }
        }
    }

    private void doLoadConfig(String configLocation){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler(HttpServletRequest req){
        if (handlerMapping.isEmpty()){
            return null;
        }
        final String requestURI = req.getRequestURI();
        for (Handler handler : handlerMapping) {
            final Pattern pattern = handler.getPattern();
            final Matcher matcher = pattern.matcher(requestURI);
            if (matcher.matches()){
                return handler;
            }
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Handler handler = getHandler(req);
        if (handler == null){
            resp.getWriter().write("404 not found!");
            return;
        }
        final Object controller = handler.getController();
        final Method method = handler.getMethod();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        final Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            final String join = StringUtils.join(entry.getValue(), ",");
            if (handler.getParamIndexMapping().containsKey(entry.getKey())){
                Integer idx = handler.getParamIndexMapping().get(entry.getKey());
                paramValues[idx] = join;
            }
        }
        Integer reqIdx = handler.getParamIndexMapping().get(HttpServletRequest.class.getName());
        if (reqIdx > 0){
            paramValues[reqIdx] = req;
        }
        Integer respIdx = handler.getParamIndexMapping().get(HttpServletResponse.class.getName());
        if (respIdx > 0){
            paramValues[respIdx] = resp;
        }
        if (interceptors != null && interceptors.size() > 0){
            for (Interceptor interceptor : interceptors) {
                boolean intercept = interceptor.intercept(req, resp, handler);
                if (intercept){
                    return;
                }
            }
        }
        try {
            method.invoke(controller, paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
