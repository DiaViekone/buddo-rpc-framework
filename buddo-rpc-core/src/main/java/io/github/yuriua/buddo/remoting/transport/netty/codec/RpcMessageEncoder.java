package io.github.yuriua.buddo.remoting.transport.netty.codec;



import io.github.yuriua.buddo.compress.Compress;
import io.github.yuriua.buddo.enums.CompressTypeEnum;
import io.github.yuriua.buddo.enums.SerializationTypeEnum;
import io.github.yuriua.buddo.extension.ExtensionLoader;
import io.github.yuriua.buddo.remoting.constants.RpcConstants;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import io.github.yuriua.buddo.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * 自定义编码器
 * <pre>
 * |———————————————————————————————————————————————————————————————————————————|
 * |                              协议                               |    正文   |
 * |———————————————————————————————————————————————————————————————————————————|
 * | 魔数   |  版本  | 全文长度  | 消息类型 | 消息ID | 压缩类型 | 序列化方式 |    正文   |
 * |———————————————————————————————————————————————————————————————————————————|
 * | 4byte | 1byte |   4byte  |  1byte | 4byte |  1byte  |   1byte   | ...      |
 * |———————————————————————————————————————————————————————————————————————————|
 * |                                                                           |
 * |                              message                                      |
 * |                                                                           |
 * |———————————————————————————————————————————————————————————————————————————|
 * </pre>
 *
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    protected void encode0(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        out.writeByte(RpcConstants.VERSION);
        /*
            留4个byte写长度
            out.writerIndex(int writerIndex):设置写下标
            out.writerIndex():获取写下标
            这里是设置写下标为当前写下标+4，即留4个byte空位
         */
        out.writerIndex(out.writerIndex()+4);
        byte messageType = rpcMessage.getMessageType();
        out.writeByte(messageType);
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());
        // TODO: 这里暂时设置死为GZIP
        out.writeByte(CompressTypeEnum.GZIP.getCode());
        // TODO: 这里暂时设置死为PROTOSTUFF
        out.writeByte(SerializationTypeEnum.PROTOSTUFF.getCode());
        // 获取全文长度
        int fullLength = RpcConstants.HEAD_LENGTH;
        byte[] bodyLength = null;
        // if messageType is not heartbeat message,fullLength = head length + body length
        if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE){

        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留4个字节写长度
            out.writerIndex(out.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);//消息类型
            out.writeByte(rpcMessage.getCodec());//编码类型
            out.writeByte(CompressTypeEnum.GZIP.getCode());//压缩类型
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());//请求id
            // build full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // serialize the object
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // compress the bytes
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }

    }


}

