package io.github.yuriua.buddo.remoting.transport.netty.handler;

import io.github.yuriua.buddo.remoting.constants.RpcConstants;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import io.github.yuriua.buddo.remoting.message.dto.RpcResponse;
import io.github.yuriua.buddo.remoting.transport.netty.client.RpcNettyClient;
import io.github.yuriua.buddo.remoting.transport.netty.client.UnprocessedRequests;
import io.github.yuriua.buddo.util.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyx
 * @date 2022/1/4 21:07
 * Description: netty rpc client handler
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedRequests unprocessedRequests;
    private final RpcNettyClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(RpcNettyClient.class);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("client received the message: {}", msg);
        // 如果当前消息是一个RpcMessage。这个类只处理RpcMessage
        if (msg instanceof RpcMessage) {
            RpcMessage tmp = (RpcMessage) msg;
            int messageType = tmp.getMessageType();
            if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                log.info("heart [{}]",tmp.getData());
            } else if (messageType == RpcConstants.RESPONSE_TYPE){
                // 获取远程调用执行完毕的结果
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                // 通知
                unprocessedRequests.complete(rpcResponse);
            }
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 处理心跳包
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.WRITER_IDLE) {
            // 触发了写事件 发送心跳包
            log.info("client send heart: {}", ctx.channel().remoteAddress());
            // 拿到当前channel
            Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
            RpcMessage rpcMessage = RpcMessage.defaultRpcMessage(
                    RpcConstants.HEARTBEAT_REQUEST_TYPE,
                    (byte) 0,
                    RpcConstants.PING
            );
            channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            super.userEventTriggered(ctx,evt);
        }
    }

    /**
     * 发生异常时
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client exception: {}",cause);
        cause.printStackTrace();
    }
}
