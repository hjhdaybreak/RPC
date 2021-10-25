package com.bee.rpc.transport.netty.server;


import com.bee.rpc.transport.AbstractServer;

import com.bee.rpc.codec.MessageCodec;
import com.bee.rpc.codec.ProtocolFrameDecoder;
import com.bee.rpc.provider.ServiceProvider;
import com.bee.rpc.provider.ServiceProviderImpl;
import com.bee.rpc.registry.ServiceRegistry;
import com.bee.rpc.registry.ZookeeperServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


@Slf4j
public class NettyServer extends AbstractServer {

    public NettyServer(String host, int port) {
        this.serviceRegistry = new ZookeeperServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.host = host;
        this.port = port;
        prepareIOC();
    }


    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(10, 0, 0,  TimeUnit.SECONDS));
                            pipeline.addLast(new ProtocolFrameDecoder());
                            pipeline.addLast(new MessageCodec());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
