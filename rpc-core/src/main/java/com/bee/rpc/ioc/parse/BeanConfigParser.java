package com.bee.rpc.ioc.parse;


import com.bee.rpc.ioc.BeanDefinition;

import java.io.InputStream;
import java.util.List;

public interface BeanConfigParser {
    List<BeanDefinition> parse(InputStream inputStream);

    List<BeanDefinition> parser(String content);
}
