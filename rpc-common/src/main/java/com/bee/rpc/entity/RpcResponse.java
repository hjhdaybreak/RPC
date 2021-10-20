package com.bee.rpc.entity;

import com.bee.rpc.enums.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 提供者执行完成或出错后向消费者返回的结果对象
 *
 * @author ziyang
 */
@Data
public class RpcResponse<T> implements Serializable {

    /**
     * 响应对应的请求号
     */
    private long requestId;
    /**
     * /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应状态补充信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, long requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> fail(ResponseCode code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }

}
