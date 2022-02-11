package com.bee.rpc.ioc.enums;

/**
 * 为什么要定义ClassEnums类 ?
 * <p>
 * 因为在BeansFactory中，使用反射，对有参构造方法实例化对象，getConstructor方法需要接受参数的字节码数组。
 * 我们需要得到配置文件中的“int”、“String”对应的字节码对象。 不能使用Class.forName("String")的方式获取，所以就采用了枚举的方式
 */

public enum ClassEnums {
    STRING("String", String.class),
    INT("int", Integer.TYPE);

    private String type;
    private Class typeClazz;

    ClassEnums(String type, Class typeClazz) {
        this.type = type;
        this.typeClazz = typeClazz;
    }

    public static Class getClass(String type) {
        for (ClassEnums classEnum : ClassEnums.values()) {
            if (classEnum.type.equalsIgnoreCase(type)) {
                return classEnum.typeClazz;
            }
        }
        throw new RuntimeException("ClassEnums没有该类型");
    }
}