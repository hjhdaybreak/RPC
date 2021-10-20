package com.bee.rpc.provider;

/**
 * 保存提供服务的实例对象
 */
public interface ServiceProvider {

    <T> void addServiceProvider(T service);

    Object getServiceProvider(String serviceName);
}
