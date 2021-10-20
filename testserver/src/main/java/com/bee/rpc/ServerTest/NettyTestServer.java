package com.bee.rpc.ServerTest;


import com.bee.rpc.transport.netty.server.NettyServer;

/**
 * 测试用 Netty 服务提供者 (服务端)
 */
public class NettyTestServer {

    public static void main(String[] args) {
        NettyServer server = new NettyServer("127.0.0.1", 7777);
        server.start();
    }
}
