package io.github.yuriua.buddo.remoting.transport.netty.handler;

import io.github.yuriua.buddo.enums.CompressTypeEnum;
import io.github.yuriua.buddo.enums.RpcResponseCodeEnum;
import io.github.yuriua.buddo.enums.SerializationTypeEnum;
import io.github.yuriua.buddo.remoting.constants.RpcConstants;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import io.github.yuriua.buddo.remoting.message.dto.RpcResponse;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;
import io.github.yuriua.buddo.util.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lyx
 * @date 2022/1/2 8:08
 * Description: netty rpc server handler
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * RPC请求处理器，负责解析客户端的远程请求并调用对应服务执行返回数据
     */
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 处理远程调用
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                // 服务器接受到RPC请求消息
                log.info("server received the message: {}",msg);
                // 获取消息类型
                int messageType = ((RpcMessage) msg).getMessageType();
                // 创建响应消息包
                RpcMessage rpcMessage = new RpcMessage();
                // 设置编解码规则，可以使用配置文件自定义
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                // 设置压缩规则
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                // 判断是否是客户端发的心跳包
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    // 返回 "pong"
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    // 真正的RPC请求，获取data中的请求载体
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // rpc请求处理器通过rpcRequest找到对应的服务并执行目标方法
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("server get result {}",result.toString());
                    // 设置消息类型为：响应
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    // 检查通道是否存活并且通道是否可以写入
                    if (ctx.channel().isActive() && ctx.channel().isWritable()){
                        // 构建成功数据
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        // 构建失败数据，因为写不出去所有该数据会丢失
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                // 写出去，如果写不出去，丢弃并关闭连接
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        } finally {
            // 释放资源
            ReferenceCountUtil.release(msg);
        }
    }
}
