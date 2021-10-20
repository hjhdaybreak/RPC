package com.bee.rpc.utils;


import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;
import com.bee.rpc.enums.ResponseCode;
import com.bee.rpc.enums.RpcError;
import com.bee.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcMessageChecker {
    private RpcMessageChecker() {
    }

    public static final String INTERFACE_NAME = "interfaceName";

    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            log.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcRequest.getRequestId() != (rpcResponse.getRequestId())) {
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            log.error("调用服务失败,serviceName:{},RpcResponse:{}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
