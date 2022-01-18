package io.github.yuriua.buddo.remoting.transport.netty.handler;

import io.github.yuriua.buddo.util.SingletonFactory;
import io.github.yuriua.buddo.remoting.message.dto.AngelBeat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lyx
 * @date 2022/1/2 9:28
 * Description: 触发IdleState.READER_IDLE事件的监听器
 */
@Deprecated
@Slf4j
@ChannelHandler.Sharable
public class AngelBeatsHandler extends ChannelDuplexHandler {
    // messageType=0
    private static final AngelBeat ANGEL_BEAT = SingletonFactory.getInstance(AngelBeat.class);
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        IdleStateEvent event = (IdleStateEvent) evt;
        // 触发了读空闲事件
        if (event.state() == IdleState.READER_IDLE) {
            log.info("已经30s没有接收到客户端请求了,连接已关闭: {}"+ctx.channel().remoteAddress());
            ctx.channel().close();
        } else if (event.state() == IdleState.WRITER_IDLE){
            log.info("客户端向服务端发送了一个哔啵: {}",ctx.channel().remoteAddress());
            ctx.writeAndFlush(ANGEL_BEAT);
        }
    }
}
