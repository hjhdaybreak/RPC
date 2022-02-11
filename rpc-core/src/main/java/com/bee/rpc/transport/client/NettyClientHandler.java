package com.bee.rpc.transport.client;

import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty客户端侧处理器
 *
 * @author hjh
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse<Object>> {
    public static final Map<Long, Promise<Object>> promises = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {

        log.info(String.format("客户端接收到消息: %s", msg));
        Promise<Object> promise = promises.get(msg.getRequestId());
        if (promise != null) {
            promise.setSuccess(msg);
        } else {
            log.error("promise 为空");
        }
        ctx.channel().close();  // 关闭channel
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    //心跳机制
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 写空闲
            if (state == IdleState.WRITER_IDLE) {
                log.info("发送心跳包 [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcRequest rpcRequest = new RpcRequest();
                // 设置心跳属性表明当前rpcRequest是心跳包
                rpcRequest.setHeartBeat(true);
                // 发送心跳包,失败则关闭
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
