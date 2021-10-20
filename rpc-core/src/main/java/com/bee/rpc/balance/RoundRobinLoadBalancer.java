package com.bee.rpc.balance;


import java.util.List;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private int index = 0;

    //基于Zookeeper的轮询负载均衡策略  List<String> serviceAddresses 服务路径集合
    protected String doSelect(List<String> serviceAddresses) {
        if (index >= serviceAddresses.size()) {
            index %= serviceAddresses.size();
        }
        return serviceAddresses.get(index++);
    }

}