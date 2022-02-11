package com.bee.rpc.ioc.context;

import com.bee.rpc.ioc.factory.DefaultBeanFactory;

public interface ApplicationContext {
    DefaultBeanFactory getBeanFactory();
}
