package com.bee.rpc.transport;


import com.bee.rpc.ioc.IocConfiguration;
import com.bee.rpc.ioc.context.ApplicationContext;
import com.bee.rpc.ioc.factory.DefaultBeanFactory;
import com.bee.rpc.provider.ServiceProvider;
import com.bee.rpc.registry.ServiceRegistry;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class AbstractServer implements RpcServer {
    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;
    protected String host;
    protected int port;


    @SuppressWarnings("unchecked")
    protected void runIoc() {
        ApplicationContext context = IocConfiguration.start();
        DefaultBeanFactory defaultBeanFactory = context.getBeanFactory();
        List<String> beanDefinitionNames = defaultBeanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object service = defaultBeanFactory.getBean(beanDefinitionName);
            publishService(service, service.getClass().getInterfaces()[0]);
        }
    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }
}
