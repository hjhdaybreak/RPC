package com.bee.rpc.transport;

import com.bee.rpc.entity.RpcRequest;

/**
 * 客户端类通用接口
 * @author hjh
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);

}
