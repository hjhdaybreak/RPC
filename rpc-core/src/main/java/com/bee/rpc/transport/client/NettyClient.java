package com.bee.rpc.transport.client;


import com.bee.rpc.balance.HashUtils;
import com.bee.rpc.transport.RpcClient;

import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;
import com.bee.rpc.registry.ServiceRegistry;
import com.bee.rpc.registry.ZookeeperServiceRegistry;
import com.bee.rpc.utils.RpcMessageChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class NettyClient implements RpcClient {

    private final ServiceRegistry serviceRegistry;

    /**
     * 用于接收消息
     */

    public NettyClient() {
        this.serviceRegistry = new ZookeeperServiceRegistry();
    }


    public Object sendRequest(RpcRequest rpcRequest) {
        Object result = null;
        try {
            InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName(), HashUtils.getHash(rpcRequest.getParameters()));
            Channel channel = ChannelProvider.get(inetSocketAddress);
            // 准备一个Promise 一个执行器 ,里面的线程接结果
            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());
            NettyClientHandler.promises.put(rpcRequest.getRequestId(), promise);
            if (channel.isActive()) {
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info(String.format("客户端发送消息: %s", rpcRequest));
                    } else {
                        future.channel().close();
                        log.error("发送消息时出现错误： ", future.cause());
                    }
                });
            }
            //等待promise结果 sync 出现异常会抛出异常
//            promise.await(1000, TimeUnit.MILLISECONDS);
            promise.await();
            if (promise.isSuccess()) {
                RpcResponse rpcResponse = (RpcResponse) promise.getNow();
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result = (rpcResponse.getData());
            } else {
                log.info("服务端返回结果为空");
            }
        } catch (Exception e) {
            log.info("发送消息时候出错: {}", e.getMessage());
        }
        return result;
    }
}

