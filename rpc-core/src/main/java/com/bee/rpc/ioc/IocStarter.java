package com.bee.rpc.ioc;


import com.bee.rpc.ioc.context.ApplicationContext;
import com.bee.rpc.ioc.context.ClassPathXmlApplicationContext;

public class IocStarter {
    public static ApplicationContext refresh() {
        return new ClassPathXmlApplicationContext("applicationContext.xml");
    }
}
