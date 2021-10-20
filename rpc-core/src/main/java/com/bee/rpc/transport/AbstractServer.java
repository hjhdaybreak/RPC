package com.bee.rpc.transport;


import com.bee.rpc.ioc.IocStarter;
import com.bee.rpc.ioc.context.ApplicationContext;
import com.bee.rpc.provider.ServiceProvider;
import com.bee.rpc.registry.ServiceRegistry;

import java.net.InetSocketAddress;
import java.util.Set;

public abstract class AbstractServer implements RpcServer {
    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;
    protected String host;
    protected int port;

    @SuppressWarnings("unchecked")
    protected void prepareIOC() {
        ApplicationContext context = IocStarter.refresh();
        Set<String> beanNames = (Set<String>) context.getBeanNames();
        for (String beanName : beanNames) {
            Object service = context.getBean(beanName);
            publishService(service, service.getClass().getInterfaces()[0]);
        }
    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }
}
