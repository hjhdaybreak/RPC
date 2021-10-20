package com.bee.rpc.registry;

import com.bee.rpc.balance.ConsistentHashingWithVirtualNode;
import com.bee.rpc.balance.HashAlgorithmUtils;
import com.bee.rpc.enums.RpcError;
import com.bee.rpc.exception.RpcException;
import com.bee.rpc.utils.IdGenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;


/**
 * 使用zookeeper作为注册中心
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {


    private static final String SERVER_ADDER = "172.20.10.13:2181,172.20.10.13:2182,172.20.10.13:2183";
    private static final ZooKeeper zooKeeper;
    private static final String SERVER_PATH_PREFIX = "/rpc/service";
    private static final String SPILT = "/";
    //参考的小灰灰的 雪花算法
    IdGenerator idGenerator = IdGenerator.getInstance();
    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, InetSocketAddress>> cache = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ConsistentHashingWithVirtualNode> consistencyMap = new ConcurrentHashMap<>();

    static {
        try {
            zooKeeper = new ZooKeeper(SERVER_ADDER, 20000, new Watch());
        } catch (IOException e) {
            log.error("连接zookeeper失败 ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    /**
     * @param serviceName
     * @param inetSocketAddress
     * @param
     */


    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            byte[] bytes = (inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort()).getBytes(StandardCharsets.UTF_8);
            Stat exists = zooKeeper.exists(SERVER_PATH_PREFIX + SPILT + serviceName, false);
            long randomUseByPath = idGenerator.nextId();
            if (exists == null) {
                zooKeeper.create(SERVER_PATH_PREFIX + SPILT + serviceName, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zooKeeper.create(SERVER_PATH_PREFIX + SPILT + serviceName + SPILT + randomUseByPath, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            } else {
                zooKeeper.create(SERVER_PATH_PREFIX + SPILT + serviceName + SPILT + randomUseByPath, bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }
            log.info("服务注册成功 {} ", serviceName);
        } catch (KeeperException | InterruptedException e) {
            log.error("注册服务出错 ", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }
    /**
     * 一致性hash 使用负载均衡算法替换 构建本地 一致性hash 映射
     * 第一次拉取 的时候 构建,后续 但节点发生变动的时候用Watch机制,删除本地建立的一致性hash
     *
     * @param serviceName 服务名称
     * @return
     */
    @Override
    public InetSocketAddress lookupService(String serviceName, int requestHashCode) {
        try {
            if (!consistencyMap.containsKey(serviceName)) {
                consistencyMap.put(serviceName, new ConsistentHashingWithVirtualNode());
            }
            ConsistentHashingWithVirtualNode consist = consistencyMap.get(serviceName);
            ConcurrentHashMap<String, InetSocketAddress> serviceAndInetMap = cache.get(serviceName);
            if (serviceAndInetMap == null) {
                serviceAndInetMap = new ConcurrentHashMap<>();
                List<String> instancesPath = zooKeeper.getChildren(SERVER_PATH_PREFIX + SPILT + serviceName, null);
                for (String s : instancesPath) {
                    // rehash
                    consist.addGroup(s);
                    String checkedChildrenPath = SERVER_PATH_PREFIX + SPILT + serviceName + SPILT + s;
                    byte[] serviceIpAndPort = zooKeeper.getData(checkedChildrenPath, null, null);
                    String[] data = new String(serviceIpAndPort).split(":");
                    serviceAndInetMap.put(s, new InetSocketAddress(data[0], Integer.parseInt(data[1])));
                }
                cache.put(serviceName, serviceAndInetMap);
                // 第一次注册一个监听器
                zooKeeper.getChildren(SERVER_PATH_PREFIX + SPILT + serviceName, new Watch(zooKeeper, SERVER_PATH_PREFIX, serviceName));
            }
            int hash = HashAlgorithmUtils.FNV1_32_HASH.getHash(Integer.toString(requestHashCode));
            String path = consist.getServer(Integer.toString(hash));
            return serviceAndInetMap.get(path);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取注册节点失败", e);
        }
        return null;
    }
}
