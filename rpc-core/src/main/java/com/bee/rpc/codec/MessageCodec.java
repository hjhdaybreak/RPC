package com.bee.rpc.codec;

import com.bee.rpc.entity.RpcRequest;
import com.bee.rpc.entity.RpcResponse;
import com.bee.rpc.enums.MessageType;
import com.bee.rpc.enums.RpcError;
import com.bee.rpc.exception.RpcException;
import com.bee.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class MessageCodec extends ByteToMessageCodec<Object> {
    private static final Logger log = LoggerFactory.getLogger(MessageCodec.class);

    private static final int MAGIC_NUMBER = -1412776739;

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER);
        if (o instanceof RpcRequest) {
            out.writeByte(MessageType.REQUEST_Message.getCode());
        } else {
            out.writeByte(MessageType.RESPONSE_Message.getCode());
        }
        //序列化算法编号
        out.writeByte(0);
        byte[] bytes = Serializer.Algorithm.ByJSON.serialize(o);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            log.error("不识别的协议包: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        byte messageType = in.readByte();
        byte serializerAlgorithmCode = in.readByte();
        int length = in.readInt();
        Class<?> messageClass;
        if (Objects.equals(MessageType.REQUEST_Message.getCode(), messageType)) {
            messageClass = RpcRequest.class;
        } else if (Objects.equals(MessageType.RESPONSE_Message.getCode(), messageType)) {
            messageClass = RpcResponse.class;
        } else {
            log.error("不识别的数据包: {}", messageType);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithmCode];
        if (algorithm == null) {
            log.error("不识别的反序列化器: {}", serializerAlgorithmCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object object = algorithm.deserialize(messageClass, bytes);
        out.add(object);
    }
}
