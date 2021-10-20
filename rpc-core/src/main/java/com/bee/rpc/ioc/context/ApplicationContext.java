package com.bee.rpc.ioc.context;

public interface ApplicationContext {

    Object getBean(String beanId);

    Object getBeanNames();
}
