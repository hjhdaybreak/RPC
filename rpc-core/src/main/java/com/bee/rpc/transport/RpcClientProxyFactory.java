package com.bee.rpc.transport;

import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC客户端动态代理
 *
 * @author hjh
 */
public class RpcClientProxyFactory implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxyFactory.class);

    private final RpcClient client;

    IdGenerator idGenerator = IdGenerator.getInstance();

    public RpcClientProxyFactory(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //方法属于的类就是接口         method.getParameterTypes() 根据参数去查找方法
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = new RpcRequest(idGenerator.nextId(), method.getDeclaringClass().getName(),
                method.getName(), method.getParameterTypes(), args, false);
        return client.sendRequest(rpcRequest);
    }
}
