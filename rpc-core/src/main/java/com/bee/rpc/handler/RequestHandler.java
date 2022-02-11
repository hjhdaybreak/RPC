package com.bee.rpc.handler;

import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;
import com.bee.rpc.enums.ResponseCode;
import com.bee.rpc.provider.ServiceProvider;
import com.bee.rpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 *
 * @author hjh
 */
@Slf4j
public class RequestHandler {
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    public Object handle(RpcRequest rpcRequest) {
        Object result = null;
        try {
            Object service = RequestHandler.serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());

            result = invokeTargetMethod(rpcRequest, service);
            log.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用或发送时有错误发生：", e);
        }
        return result;
    }

    /**
     *
     * @param rpcRequest
     * @param service 具体方法 
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException {
        Method method;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }

}
