package com.bee.rpc.ioc.factory;


import java.util.List;

public interface BeanFactory {


    public Object getBean(String beanId);

    public List<String> getBeanDefinitionNames();
}
