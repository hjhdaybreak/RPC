package com.bee.rpc.balance;

import java.util.List;

public class RandomBalancer {
    private int index = 0;

    /**
     * @param serviceAddresses 服务路径集合
     * @return
     */
    protected String doSelect(List<String> serviceAddresses) {
        if (index >= serviceAddresses.size()) {
            index %= serviceAddresses.size();
        }
        return serviceAddresses.get(index++);
    }

}
