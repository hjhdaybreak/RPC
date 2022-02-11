package com.bee.rpc.balance;

import java.util.*;

/**
 * 在引入足够多的虚拟节点后, 一致性hash还是能够比较完美地满足负载均衡需要的。
 * https://blog.csdn.net/weixin_42864905/article/details/105635577
 */

public class ConsistentHashingWithVirtualNode implements LoadBalancer {

    /**
     * 真实集群列表
     */

    private List<String> realGroups = new LinkedList<>();

    /**
     * 虚拟节点映射关系
     */
    private SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    private final int VIRTUAL_NODE_NUM = 1000;

    private static String getVirtualNodeName(String realName, int num) {
        return realName + "&&VN" + (num);
    }

    private String getRealNodeName(String virtualName) {
        return virtualName.split("&&")[0];
    }

    public String getServer(int hash) {

        // 只取出所有大于该hash值的部分而不必遍历整个 Tree
        // 返回此地图部分的视图, 其键大于等于 fromKey
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNodeName;
        if (subMap.isEmpty()) {
            // hash值在最尾部,应该映射到第一个group上
            virtualNodeName = virtualNodes.get(virtualNodes.firstKey());
        } else {
            /**
             * 返回此地图中当前的第一个 (最低) 键
             */
            virtualNodeName = subMap.get(subMap.firstKey());
        }
        return getRealNodeName(virtualNodeName);
    }


    public void refreshHashCircle() {
        // 当集群变动时,刷新hash环,其余的集群在hash环上的位置不会发生变动
        virtualNodes.clear();
        for (String realGroup : realGroups) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNodeName = getVirtualNodeName(realGroup, i);
                int hash = HashUtils.getHash(virtualNodeName);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }

    public void addGroup(String identifier) {
        realGroups.add(identifier);
    }

    public void removeGroup(String identifier) {
        int i = 0;
        for (String group : realGroups) {
            if (group.equals(identifier)) {
                realGroups.remove(i);
            }
            i++;
        }
    }

}
