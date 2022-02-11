package com.bee.rpc.provider;

import com.bee.rpc.enums.RpcError;
import com.bee.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存在本地的服务注册表
 */

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    @Override
    public <T> void addServiceProvider(T service) {
        //service 具体实现类 xxxImpl
        //serviceName 是接口具体实现类的名字
        String serviceName = service.getClass().getCanonicalName();
        if (registeredService.contains(serviceName))
            return;
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        //接口具体实现类实现的接口
        if (interfaces.length == 0)
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        if (interfaces.length > 1)
            throw new RpcException(RpcError.SERVICE_IMPLEMENT_TO_MUCH_INTERFACE);
        //本地注册的是服务 保存形式是以 key为接口 value具体实现类
        serviceMap.put(interfaces[0].getCanonicalName(), service);
        log.info("向接口: {} 注册服务: {}", interfaces, serviceName);
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null)
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        return service;
    }
}
