package com.bee.rpc.clientTest;

import com.bee.rpc.api.HelloObject;
import com.bee.rpc.api.HelloService;
import com.bee.rpc.transport.RpcClient;
import com.bee.rpc.transport.RpcClientProxyFactory;
import com.bee.rpc.transport.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用Netty 消费者
 */
@Slf4j
public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        RpcClientProxyFactory rpcClientProxyFactory = new RpcClientProxyFactory(client);
        HelloService proxyObject = rpcClientProxyFactory.getProxy(HelloService.class);
        log.info(" hello 结果是 {}", proxyObject.hello(new HelloObject(1, "This is a message")));
    }
}