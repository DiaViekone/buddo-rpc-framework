package io.github.yuriua.buddo.remoting.message.dto;


import io.github.yuriua.buddo.enums.CompressTypeEnum;
import io.github.yuriua.buddo.enums.SerializationTypeEnum;
import io.github.yuriua.buddo.remoting.message.Message;
import io.github.yuriua.buddo.util.SingletonFactory;
import lombok.*;

/**
 * RPC消息体
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    //rpc message type（消息类型）
    private byte messageType;
    //serialization type（序列化类型）
    private byte codec;
    //compress type （压缩类型）
    private byte compress;
    //request id（请求id）
    private int requestId;
    //request data（请求数据 RpcRequest）
    private Object data;



    /**
     * 获取一个使用默认序列化、压缩算法的RpcMessage
     * @param messageType 消息类型
     * @param requestId 请求id号
     * @param data 数据负载
     * @return
     */
    public static RpcMessage defaultRpcMessage(byte messageType,byte requestId,Object data) {
        return new RpcMessage(
                messageType,
                SerializationTypeEnum.PROTOSTUFF.getCode(),
                CompressTypeEnum.GZIP.getCode(),
                requestId,
                data
        );
    }
}
