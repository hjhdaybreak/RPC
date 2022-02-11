package com.bee.rpc.ioc.factory;


import com.bee.rpc.ioc.BeanDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.提供获取 Bean 对象的方法
 * 2.根据 beanDefinitions 创建对象
 */
public class DefaultBeanFactory implements BeanFactory {
    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final List<String> beanDefinitionsName = new ArrayList<>();

    public void addBeanDefinitions(List<BeanDefinition> beanDefinitions) {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            this.beanDefinitions.putIfAbsent(beanDefinition.getId(), beanDefinition);
            beanDefinitionsName.add(beanDefinition.getId());
        }
        //单例并且没有延迟初始化
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (!beanDefinition.isLazyInit() && beanDefinition.isSingleton()) {
                createBean(beanDefinition);
            }
        }
    }

    private Object createBean(BeanDefinition beanDefinition) {
        // id 唯一
        if (beanDefinition.isSingleton() && singletonObjects.containsKey(beanDefinition.getId())) {
            return singletonObjects.get(beanDefinition.getId());
        }
        Object bean = null;
        try {
            Class<?> beanClass = Class.forName(beanDefinition.getClassName());
            List<BeanDefinition.ConstructorArg> args = beanDefinition.getConstructorArgs();
            if (args.isEmpty()) {
                bean = beanClass.newInstance();
            } else {
                Class<?>[] argClasses = new Class[args.size()];
                Object[] argObjects = new Object[args.size()];
                for (int i = 0; i < args.size(); ++i) {
                    BeanDefinition.ConstructorArg arg = args.get(i);
                    if (!arg.getIsRef()) {
                        argClasses[i] = arg.getType();
                        argObjects[i] = arg.getValue();
                    } else {
                        BeanDefinition refBeanDefinition = beanDefinitions.get(arg.getRef());
                        if (refBeanDefinition == null) {
                            throw new RuntimeException("Bean is not defined: " + arg.getValue());
                        }
                        argClasses[i] = Class.forName(refBeanDefinition.getClassName());
                        argObjects[i] = createBean(refBeanDefinition);
                    }
                }
                //反射,根据有参构造方法创建对象
                bean = beanClass.getConstructor(argClasses).newInstance(argObjects);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (bean != null && beanDefinition.isSingleton()) {
            singletonObjects.putIfAbsent(beanDefinition.getId(), bean);
            return singletonObjects.get(beanDefinition.getId());
        }
        return bean;
    }

    @Override
    public Object getBean(String beanId) {
        BeanDefinition beanDefinition = beanDefinitions.get(beanId);
        if (beanDefinition == null) {
            throw new RuntimeException("bean is not defined，" + beanId);
        }
        return createBean(beanDefinition);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return beanDefinitionsName;
    }
}
