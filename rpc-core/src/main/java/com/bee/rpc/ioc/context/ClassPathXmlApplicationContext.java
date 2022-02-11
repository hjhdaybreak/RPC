package com.bee.rpc.ioc.context;


import com.bee.rpc.ioc.BeanDefinition;
import com.bee.rpc.ioc.factory.DefaultBeanFactory;
import com.bee.rpc.ioc.parse.BeanConfigParser;
import com.bee.rpc.ioc.parse.XmlBeanConfigParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 1.组装 beansFactory 和 beanConfigParser
 * 2.串联执行流程,读取配置文件,通过beanConfigParser解析到 beanDefinitions
 * 3.beansFactory 根据 beanDefinitions 加载对象
 */

public class ClassPathXmlApplicationContext implements ApplicationContext {

    public DefaultBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    private DefaultBeanFactory beanFactory;
    private BeanConfigParser beanConfigParser;

    public ClassPathXmlApplicationContext(String configLocation) {
        this.beanFactory = new DefaultBeanFactory();
        this.beanConfigParser = new XmlBeanConfigParser();
        loadBeanDefinitions(configLocation);
    }

    /**
     * 配置文件解析,并将配置信息读取转化到 BeanDefinition集合
     *
     * @param configLocation
     */

    private void loadBeanDefinitions(String configLocation) {
        InputStream in = null;
        try {
            in = this.getClass().getResourceAsStream("/" + configLocation);
            if (in == null) {
                throw new RuntimeException("找不到配置文件:" + configLocation);
            }
            List<BeanDefinition> beanDefinitions = beanConfigParser.parse(in);
            beanFactory.addBeanDefinitions(beanDefinitions);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
