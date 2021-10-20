package com.bee.rpc.balance;

import java.util.*;

/**
 * 在引入足够多的虚拟节点后, 一致性hash还是能够比较完美地满足负载均衡需要的。
 */

public class ConsistentHashingWithVirtualNode implements LoadBalancer {
//    /**
//     * 集群地址列表
//     */
//    private static String[] groups = {
//    };

//    private static final String SERVER_ADDER = "192.168.0.4:2181,192.168.0.4:2182,192.168.0.4:2183";

    /**
     * 真实集群列表
     */

    private  List<String> realGroups = new LinkedList<>();

    /**
     * 虚拟节点映射关系
     */
    private  SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    private  final int VIRTUAL_NODE_NUM = 1000;


//    static {
//
//        // 先添加真实节点列表
//        realGroups.addAll(Arrays.asList(groups));
//        // 将虚拟节点映射到Hash环上
//        for (String realGroup : realGroups) {
//            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
//                String virtualNodeName = getVirtualNodeName(realGroup, i);
//                int hash = HashAlgorithmUtils.FNV1_32_HASH.getHash(virtualNodeName);
//                virtualNodes.put(hash, virtualNodeName);
//            }
//        }
//    }

    private static   String getVirtualNodeName(String realName, int num) {
        return realName + "&&VN" + (num);
    }

    private  String getRealNodeName(String virtualName) {
        return virtualName.split("&&")[0];
    }

    public  String getServer(String key) {
        int hash = HashAlgorithmUtils.FNV1_32_HASH.getHash(key);
        // 只取出所有大于该hash值的部分而不必遍历整个 Tree
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNodeName;
        if (subMap.isEmpty()) {
            // hash值在最尾部,应该映射到第一个group上
            virtualNodeName = virtualNodes.get(virtualNodes.firstKey());
        } else {
            virtualNodeName = subMap.get(subMap.firstKey());
        }
        return getRealNodeName(virtualNodeName);
    }


    private  void refreshHashCircle() {
        // 当集群变动时,刷新hash环,其余的集群在hash环上的位置不会发生变动
        virtualNodes.clear();
        for (String realGroup : realGroups) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNodeName = getVirtualNodeName(realGroup, i);
                int hash = HashAlgorithmUtils.FNV1_32_HASH.getHash(virtualNodeName);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }

    public  void addGroup(String identifier) {
        realGroups.add(identifier);
        refreshHashCircle();
    }

    public  void removeGroup(String identifier) {
        int i = 0;
        for (String group : realGroups) {
            if (group.equals(identifier)) {
                realGroups.remove(i);
            }
            i++;
        }
        refreshHashCircle();
    }

}
