package com.bee.rpc.clientTest;

import com.bee.rpc.api.HelloObject;
import com.bee.rpc.api.HelloService;
import com.bee.rpc.api.SeeYouObject;
import com.bee.rpc.api.SeeYouService;
import com.bee.rpc.transport.RpcClient;
import com.bee.rpc.transport.RpcClientProxy;
import com.bee.rpc.transport.netty.client.NettyClient;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 测试用Netty消费者
 */
public class NettyTestClient {

    public static void main(String[] args) throws InterruptedException {
        RpcClient client = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(1, "This is a message");
        String res = helloService.hello(object);
        System.out.println("hello 结果是" + res);

////        for (int i = 0; i <20; i++) {
////            Taske taske = new Taske(rpcClientProxy, client);
////            Thread thread = new Thread(taske);
////            thread.start();
////        }
////        System.out.println("--------------------------------------------------------------");
////        SeeYouService seeYouService = rpcClientProxy.getProxy(SeeYouService.class);
////        SeeYouObject seeYouObject = new SeeYouObject(13, "seeYou");
////        String result = seeYouService.seeYou(seeYouObject);
////        System.out.println("see you 结果是" + result);
//    }
//
//    static class Taske implements Runnable {
//        RpcClientProxy rpcClientProxy;
//        RpcClient client;
//        HelloService helloService;
//
//        public Taske(RpcClientProxy rpcClientProxy, RpcClient client) {
//            this.rpcClientProxy = rpcClientProxy;
//            this.client = client;
//            helloService = rpcClientProxy.getProxy(HelloService.class);
//        }
//        @Override
//        public void run() {
//
//            HelloObject object = new HelloObject(1, "This is a message");
//            String res = helloService.hello(object);
//            System.out.println("hello 结果是" + res);
//        }
    }
}


