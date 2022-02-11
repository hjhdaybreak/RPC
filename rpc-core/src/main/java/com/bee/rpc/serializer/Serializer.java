package com.bee.rpc.serializer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bee.rpc.entity.RpcRequest;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import io.protostuff.Schema;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
    /*
        这里由于使用JSON序列化和反序列化Object数组，无法保证反序列化后仍然为原实例类型
        需要重新判断处理
     */
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
        },
        ByProtobuf {
            private Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();
            private LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

            @Override
            public Object deserialize(Class<?> clazz, byte[] bytes) {
                Schema schema = getSchema(clazz);
                Object obj = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, obj, schema);
                return obj;
            }

            @Override
            public <T> byte[] serialize(T obj) {
                Class clazz = obj.getClass();
                Schema schema = getSchema(clazz);
                byte[] data = ProtobufIOUtil.toByteArray(obj, schema, buffer);
                buffer.clear();
                return data;
            }

            private Schema getSchema(Class clazz) {
                Schema schema = schemaCache.get(clazz);
                if (Objects.isNull(schema)) {
                    // 这个schema通过RuntimeSchema进行懒创建并缓存
                    // 所以可以一直调用RuntimeSchema.getSchema(),这个方法是线程安全的
                    schema = RuntimeSchema.getSchema(clazz);
                    if (Objects.nonNull(schema)) {
                        schemaCache.put(clazz, schema);
                    }
                }
                return schema;
            }
        }
    }
}
