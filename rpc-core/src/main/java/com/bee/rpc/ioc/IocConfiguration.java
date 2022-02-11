package com.bee.rpc.ioc;


import com.bee.rpc.ioc.context.ApplicationContext;
import com.bee.rpc.ioc.context.ClassPathXmlApplicationContext;

/**
 * 掌握了dom4j
 * 学会了工厂模式设计思想
 * 深刻理解了DI框架设计原理
 * 加深了对面向对象设计思想的理解 比如, 使用BeanDefinition 接收配置解析信息,
 * ClassPathXmlApplicationContext 组装对象,串联流程,让类的职责、代码流程更加清晰
 */

public class IocConfiguration {
    public static ApplicationContext start() {
        return new ClassPathXmlApplicationContext("applicationContext.xml");
    }
}
