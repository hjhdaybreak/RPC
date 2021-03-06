package com.bee.rpc.transport.server;


import com.bee.rpc.aop.interceptor.AfterReturnInterceptor;
import com.bee.rpc.aop.interceptor.BeforeInterceptor;
import com.bee.rpc.aop.interceptor.ExceptionInterceptor;
import com.bee.rpc.handler.RequestHandler;
import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;

import com.bee.rpc.limiter.LeakyBucketLimiter;
import com.bee.rpc.utils.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final RequestHandler requestHandler;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler";
    private static final ExecutorService threadPool;
    private static final LeakyBucketLimiter leakyBucketLimiter;

    static {
        requestHandler = new RequestHandler();
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
        leakyBucketLimiter = new LeakyBucketLimiter(1);
    }


    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        // 如果HeartBeat属性为true，那么该消息是心跳包
        if (msg.getHeartBeat()) {
            log.info("接收到客户端心跳包...");
            return;
        }
        threadPool.execute(() -> {
            Object result;
            try {
                log.info(String.format("客户端接收到消息: %s", msg));
                BeforeInterceptor beforeInterceptor = new BeforeInterceptor() {
                    @Override
                    public Object before(Object proxy, Method method, Object[] args) {
                        return leakyBucketLimiter.acquire();
                    }
                };
                boolean permit = (boolean) beforeInterceptor.before(null, null, null);
                if (permit) {
                    result = requestHandler.handle(msg);
                } else {
                    result = "当前服务器繁忙,请稍后重试!";
                }
                AfterReturnInterceptor afterReturnInterceptor = new AfterReturnInterceptor() {
                    @Override
                    public Object after(Object proxy, Method method, Object[] args, Object returnResult) {
                        return null;
                    }
                };
                afterReturnInterceptor.after(null, null, null, result);
                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
                future.addListener(ChannelFutureListener.CLOSE);
            } catch (Exception e) {
                ExceptionInterceptor exceptionInterceptor = new ExceptionInterceptor() {
                    @Override
                    public void intercept(Object proxy, Method method, Object[] args, Throwable throwable) {
                    }
                };
                exceptionInterceptor.intercept(null, null, null, null);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("过程调用时有错误发生:");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 关闭了");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("长时间未收到心跳包，断开连接...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
