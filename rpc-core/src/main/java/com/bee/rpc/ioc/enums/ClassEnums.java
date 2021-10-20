package com.bee.rpc.ioc.enums;

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
            if (classEnum.type.equalsIgnoreCase(type)){
                return classEnum.typeClazz;
            }
        }
        throw new RuntimeException("ClassEnums没有该类型");
    }
}