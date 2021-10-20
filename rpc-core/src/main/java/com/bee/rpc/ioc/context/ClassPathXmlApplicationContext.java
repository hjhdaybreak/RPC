package com.bee.rpc.ioc.context;




import com.bee.rpc.ioc.BeanDefinition;
import com.bee.rpc.ioc.factory.BeansFactory;
import com.bee.rpc.ioc.parse.BeanConfigParser;
import com.bee.rpc.ioc.parse.XmlBeanConfigParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassPathXmlApplicationContext implements ApplicationContext {
    private BeansFactory beansFactory;
    private BeanConfigParser beanConfigParser;
    private Set<String> beanNames;

    public ClassPathXmlApplicationContext(String configLocation) {
        this.beansFactory = new BeansFactory();
        this.beanConfigParser = new XmlBeanConfigParser();
        beanNames = new HashSet<>();
        loadBeanDefinitions(configLocation);

    }

    private void loadBeanDefinitions(String configLocation) {
        InputStream in = null;
        try {
            in = this.getClass().getResourceAsStream("/" + configLocation);
            if (in == null) {
                throw new RuntimeException("找不到配置文件:" + configLocation);
            }
            List<BeanDefinition> beanDefinitions = beanConfigParser.parse(in);

            for (BeanDefinition beanDefinition : beanDefinitions) {
                beanNames.add(beanDefinition.getId());
            }

            beansFactory.addBeanDefinitions(beanDefinitions);
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

    @Override
    public Object getBean(String beanId) {
        return beansFactory.getBean(beanId);
    }

    @Override
    public Set<String> getBeanNames() {
        return beanNames;
    }
}
