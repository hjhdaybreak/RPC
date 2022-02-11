package com.bee.rpc.ioc.parse;


import com.bee.rpc.ioc.BeanDefinition;
import com.bee.rpc.ioc.enums.ClassEnums;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 配置文件内容解析成 BeanDefinition 集合对象
 */
public class XmlBeanConfigParser implements BeanConfigParser {

    @Override
    @SuppressWarnings("unchecked")
    public List<BeanDefinition> parse(InputStream inputStream) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        //dom4j
        SAXReader saxReader = new SAXReader();
        Document document;
        try {
            document = saxReader.read(inputStream);
            //获取bean节点集合
            Element root = document.getRootElement();
            List<Element> elements = root.selectNodes("bean");
            if (elements == null || elements.size() == 0) {
                throw new RuntimeException("no find bean tag !");
            }

            for (Element element : elements) {
                BeanDefinition beanDefinition = new BeanDefinition();
                //获取id和class属性值
                String id = element.attributeValue("id");
                String className = element.attributeValue("class");
                List<Element> childElements = element.elements("constructor-arg");
                List<BeanDefinition.ConstructorArg> constructorArgs = new ArrayList<>();
                for (Element childElement : childElements) {
                    BeanDefinition.ConstructorArg constructorArg = new BeanDefinition.ConstructorArg();
                    String ref = childElement.attributeValue("ref");


                    String typeClassName = childElement.attributeValue("type");
                    Class<?> typeClazz = null;
                    if (typeClassName != null) {
                        typeClazz = ClassEnums.getClass(typeClassName);
                    }
                    //获取依赖的对象,可能没有
                    constructorArg.setRef(ref);
                    constructorArg.setType(typeClazz);
                    if (childElement.attributeValue("value") != null) {
                        //如果是int类型,需要将类型转换,转化为Integer类型
                        if ("int".equals(typeClassName)) {
                            constructorArg.setValue(Integer.valueOf(childElement.attributeValue("value")));
                        } else {
                            constructorArg.setValue(childElement.attributeValue("value"));
                        }
                    }
                    constructorArgs.add(constructorArg);
                }

                if (element.attributeValue("scope") != null) {
                    beanDefinition.setScope(element.attributeValue("scope"));
                }

                beanDefinition.setId(id);
                beanDefinition.setClassName(className);
                beanDefinition.setConstructorArgs(constructorArgs);
                beanDefinitions.add(beanDefinition);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return beanDefinitions;
    }
}