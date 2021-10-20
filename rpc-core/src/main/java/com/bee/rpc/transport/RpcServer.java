package com.bee.rpc.transport;

/**
 * 服务器类通用接口
 *
 * @author hjh
 */

public interface RpcServer {

    void start();

    <T> void publishService(Object service, Class<T> serviceClass);
}
