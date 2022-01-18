package io.github.yuriua.buddo.remoting.transport.netty.codec;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.github.yuriua.buddo.config.BuddoConfig;
import io.github.yuriua.buddo.enums.CompressTypeEnum;
import io.github.yuriua.buddo.enums.SerializationTypeEnum;
import io.github.yuriua.buddo.remoting.message.Message;
import io.github.yuriua.buddo.remoting.constants.RpcConstants;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义编解码器
 * <pre>
 * /————————————————————————————————————————————————————————————————————————
 * | 魔数   | 版本   | 序列化方式 | 消息类型 | 消息ID | 压缩类型 | 正文长度 | 正文   |
 * |————————————————————————————————————————————————————————————————————————
 * | 4byte | 1byte |   1byte   | 1byte   | 4byte | 1byte  | 4byte   | ...  |
 * |————————————————————————————————————————————————————————————————————————
 * |                                                                       |
 * |                              message                                  |
 * |                                                                       |
 * |————————————————————————————————————————————————————————————————————————
 * </pre>
 */
@Deprecated
@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, RpcMessage> {

    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    /**
     * 消息出站时进行编码
     * @param ctx
     * @param msg
     * @param outList
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, List<Object> outList) throws Exception {
        // 获取消息类型
        int messageType = msg.getMessageType();
        // ###开始编码###
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4 字节的魔数
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        // 2. 1 字节的版本
        out.writeByte(RpcConstants.VERSION);
        // 3. 1 字节的序列化方式
        out.writeByte(msg.getCodec());
        // 4. 1 字节的消息类型
        out.writeByte(messageType);
        // 5. 4 个字节请求id
        out.writeInt(REQUEST_ID.getAndIncrement());
        // 6. 1 个字节压缩类型
        out.writeByte(CompressTypeEnum.GZIP.getCode());//TODO: 暂时不写！
        // 7. 获取内容的字节数组
        byte[] bodyBytes = null;
       // if (messageType != Message.AngelBeat && messageType != Message)
        // 8. 4 个字节消息长度
        //out.writeInt(bytes.length);
        // 9. 写入内容
        //out.writeBytes(bytes);
        outList.add(out);
    }

    /**
     * 消息入站进行解码转化为RpcMessage
     * @param ctx
     * @param msg
     * @param outList
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> outList) throws Exception {
        // 效验魔数
        checkMagicNumber(msg);
        // 效验版本号
        checkVersion(msg);
        // build RpcMessage
        // 序列化类型
        byte serializerType = msg.readByte();
        // 消息类型
        byte messageType = msg.readByte();
        // 消息id
        int requestId = msg.readInt();
        // 压缩类型
        byte conpressType = msg.readByte();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(serializerType)
                .messageType(messageType)
                .requestId(requestId).build();
        // 处理心跳包
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PING);
        }

        //int bodyLength =

        int bodyLength = msg.readInt();//消息正文长度（不包含前16个字节）
        byte[] bytes = new byte[bodyLength];
        msg.readBytes(bytes, 0, bodyLength);
        //找到message的类型
        //Class<? extends Message> messageClass = Message.getMessageClass(messageType);
    }


    /**
     * 效验消息数据版本号
     * @param in
     */
    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }


}
