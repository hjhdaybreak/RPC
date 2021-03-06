package com.bee.rpc.registry;

import com.bee.rpc.balance.ConsistentHashingWithVirtualNode;
import com.bee.rpc.enums.RpcError;
import com.bee.rpc.exception.RpcException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class Watch implements Watcher {
    ZooKeeper zooKeeper;
    String monitorPath;
    String serviceName;
    final String SPILT = "/";
    static AtomicInteger order = new AtomicInteger(1);
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, InetSocketAddress>> cache = ZookeeperServiceRegistry.cache;
    public final ConcurrentHashMap<String, ConsistentHashingWithVirtualNode> consistencyMap = ZookeeperServiceRegistry.consistencyMap;

    public Watch(ZooKeeper zooKeeper, String monitorPath, String serviceName) {
        this.zooKeeper = zooKeeper;
        this.monitorPath = monitorPath;
        this.serviceName = serviceName;
    }

    public Watch() {
    }

    /**
     * 事件通知回调方法
     *
     * @param event
     */
    @SneakyThrows
    @Override

    public void process(WatchedEvent event) {
        try {
            //事件路径
            String path = event.getPath();
            //事件状态  即  连接不连接
            KeeperState state = event.getState();
            //事件类型
            EventType type = event.getType();
            log.info("事件路径" + path + ",事件状态" + state + ",事件类型" + type);
            if (state == KeeperState.SyncConnected) {
                log.info("连接zookeeper成功");
            }
            if (EventType.NodeChildrenChanged == type) {
                getNewCache();
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("zookeeper 连接失败");
        } finally {
            int thisOrder = order.get();
            order.incrementAndGet();
            if (thisOrder != 1) {
                log.info("再次注册新Watch!!!");
                zooKeeper.getChildren(monitorPath + SPILT + serviceName, new Watch(zooKeeper, monitorPath, serviceName));
            }
        }
    }

    private void getNewCache() throws KeeperException, InterruptedException {
        // 获取当前最新的所有服务
        List<String> children = zooKeeper.getChildren(monitorPath + SPILT + serviceName, null);
        ConsistentHashingWithVirtualNode consistentHashingWithVirtualNode = consistencyMap.get(serviceName);
        Set<String> newServiceSet = new HashSet<>(children);
        ConcurrentHashMap<String, InetSocketAddress> oldServiceSet = cache.get(serviceName);

        //遍历原来的服务集合,如果组最新集合不包含,就将其移除
        for (Map.Entry<String, InetSocketAddress> entry : oldServiceSet.entrySet()) {
            if (!newServiceSet.contains(entry.getKey())) {
                consistentHashingWithVirtualNode.removeGroup(entry.getKey());
                oldServiceSet.remove(entry.getKey());
            }
        }

        newServiceSet.removeAll(cache.keySet());  // 求差集,求多出来的服务
        

        //添加多出来的服务
        if (!newServiceSet.isEmpty()) {
            for (String path : newServiceSet) {
                String checkedChildrenPath = monitorPath + SPILT + serviceName + SPILT + path;
                consistentHashingWithVirtualNode.addGroup(path);
                byte[] serviceIpAndPort = zooKeeper.getData(checkedChildrenPath, null, null);
                String[] data = new String(serviceIpAndPort).split(":");
                oldServiceSet.put(path, new InetSocketAddress(data[0], Integer.parseInt(data[1])));
            }
            //最后刷新一致性hash环
            consistentHashingWithVirtualNode.refreshHashCircle();
            //覆盖原来的缓存
            cache.put(serviceName, oldServiceSet);
        }
    }
}
