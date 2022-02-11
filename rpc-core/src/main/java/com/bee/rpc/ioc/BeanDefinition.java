package com.bee.rpc.ioc;

import lombok.Data;

import java.util.List;


/**
 * 配置文件中bean节点的映射对象
 */
@Data
public class BeanDefinition {
    private String id;
    private String className;
    private List<ConstructorArg> constructorArgs;
    private String scope = "singleton";
    private boolean lazyInit = false;

    public boolean isSingleton() {
        return "singleton".equals(this.scope);
    }

    @Data
    public static class ConstructorArg {
        private String ref;
        private Class type;
        private Object value;

        public boolean getIsRef() {
            return ref != null;
        }
    }
}
