package com.bee.rpc.serializer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bee.rpc.entity.RpcRequest;

import java.nio.charset.StandardCharsets;

public interface Serializer {
    Object deserialize(Class<?> paramClass, byte[] bytes);

    <T> byte[] serialize(T paramT);

    enum Algorithm implements Serializer {
        ByJSON {
            public Object deserialize(Class<?> clazz, byte[] bytes) {
                Object obj = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
                if (obj instanceof RpcRequest)
                    obj = handleRequest(obj);
                return obj;
            }
            private Object handleRequest(Object obj) {
                RpcRequest rpcRequest = (RpcRequest) obj;
                for (int i = 0; i < (rpcRequest.getParameterTypes()).length; i++) {
                    Class<?> clazz = rpcRequest.getParameterTypes()[i];
                    if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                        Object o = JSON.parseObject(new String(rpcRequest.getParameters()[i].toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), clazz);
                        rpcRequest.getParameters()[i] = o;
                    }
                }
                return rpcRequest;
            }

            public <T> byte[] serialize(T object) {
                return JSONObject.toJSONString(object).getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
