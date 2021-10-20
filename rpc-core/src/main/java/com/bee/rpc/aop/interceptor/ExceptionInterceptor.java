package com.bee.rpc.aop.interceptor;

import java.lang.reflect.Method;

public interface ExceptionInterceptor {

    /**
     * 异常信息
     *
     * @param proxy
     * @param method
     * @param args
     * @param throwable
     */
    void intercept(Object proxy, Method method, Object[] args, Throwable throwable);

}
