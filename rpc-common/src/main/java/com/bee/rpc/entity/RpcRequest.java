package com.bee.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 消费者向提供者发送的请求对象
 *
 * @author ziyang
 */
@Data
@AllArgsConstructor
public class RpcRequest implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcRequest that = (RpcRequest) o;
        return Arrays.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parameters);
    }

    /**
     * 请求的序号
     */
    private long requestId;

    /**
     * 待调用接口名称
     */
    private String interfaceName;

    /**
     * 待调用方法名称
     */
    private String methodName;

    /**
     * 调用方法的参数
     */
    private Object[] parameters;

    /**
     * 调用方法的参数类型
     */
    private Class<?>[] parameterTypes;

}
