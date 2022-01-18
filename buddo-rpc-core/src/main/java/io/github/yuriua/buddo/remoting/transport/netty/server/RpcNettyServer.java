package io.github.yuriua.buddo.remoting.transport.netty.server;

import io.github.yuriua.buddo.exception.BuddoException;
import io.github.yuriua.buddo.remoting.transport.netty.codec.RpcMessageDecoder;
import io.github.yuriua.buddo.remoting.transport.netty.codec.RpcMessageEncoder;
import io.github.yuriua.buddo.config.CustomShutdownHook;
import io.github.yuriua.buddo.util.RuntimeUtil;
import io.github.yuriua.buddo.util.SingletonFactory;
import io.github.yuriua.buddo.util.concurrent.threadpool.ThreadPoolFactoryUtils;
import io.github.yuriua.buddo.config.BuddoConfig;
import io.github.yuriua.buddo.config.BuddoServerConfig;
import io.github.yuriua.buddo.remoting.transport.netty.codec.MessageCodec;
import io.github.yuriua.buddo.remoting.transport.netty.handler.AngelBeatsHandler;
import io.github.yuriua.buddo.remoting.transport.netty.handler.NettyRpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author lyx
 * @date 2022/1/2 7:17
 * Description:
 */
@Slf4j
@Component
public class RpcNettyServer {
    private static final LoggingHandler LOGGING_HANDLER = SingletonFactory.getInstance(LoggingHandler.class, LogLevel.DEBUG);
    private static final AngelBeatsHandler ANGEL_BEATS_HANDLER = SingletonFactory.getInstance(AngelBeatsHandler.class);
    @Deprecated
    private static final MessageCodec MESSAGE_CODEC = SingletonFactory.getInstance(MessageCodec.class);
    //
    private static final RpcMessageEncoder RPC_MESSAGE_ENCODER = new RpcMessageEncoder();

    //@PostConstruct
    @SneakyThrows
    public void start() {

        //添加清理钩子
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
        );

        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(LOGGING_HANDLER);
                            // 参数1：最大帧长度 参数2：长度字节偏移量 参数3：长度字长 参数4：正文偏移量（长度字长后面的非正文数据） 参数5：从头剥离几个字长
                            //p.addLast(new LengthFieldBasedFrameDecoder(1024,12,4,0,16));
                            // 编码器
                            p.addLast(new RpcMessageEncoder());
                            // 解码器
                            p.addLast(new RpcMessageDecoder());
                            // 默认30秒之内没有收到客户端请求的话就关闭连接
                            p.addLast(new IdleStateHandler(BuddoServerConfig.getServerTimeout(), 0, 0, BuddoConfig.getDefaultTimeUnit()));
                            // 每隔15秒向服务端发送心跳
                            p.addLast(ANGEL_BEATS_HANDLER);
                            // 处理远程调用请求（RPC调用的主要处理器）
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            //ChannelFuture channelFuture = b.bind(BuddoServerConfig.getServerPort()).sync();
            ChannelFuture channelFuture = bind(b, BuddoServerConfig.getServerPort(),10);
            channel = channelFuture.channel();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server exception: {}", e);
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }

    Channel channel;

    public static void main(String[] args) throws InterruptedException {
        RpcNettyServer rpcNettyServer = new RpcNettyServer();
        new Thread(() -> {
            rpcNettyServer.start();
        }).start();
        TimeUnit.SECONDS.sleep(3);
        rpcNettyServer.channel.writeAndFlush("0");
    }

    /**
     * 绑定端口
     * @param b
     * @param reBindCount
     * @return
     * @throws Exception
     */
    private ChannelFuture bind(ServerBootstrap b, int port, int reBindCount) throws Exception {
        ChannelFuture channelFuture = null;
        InetSocketAddress inetSocketAddress = null;
        if (reBindCount < 0) {
            throw new BuddoException("reBindCount can't < 0");
        } else if (port < 0 || port > 65535){
            throw new BindException("port must be between 0-65535");
        }
        reBindCount--;
        try {
            inetSocketAddress = new InetSocketAddress(port);
            channelFuture = b.bind(inetSocketAddress);
        } catch (Exception e) {
            if (e instanceof BindException) {
                System.out.println(reBindCount);
                if (reBindCount == 0) {
                    throw new BuddoException("bind exception: " + e.getMessage());
                } else {
                    bind(b, ++port, reBindCount);
                }
            } else {
                throw e;
            }
        }
        log.info("server binding port succeeded: {}",port);
        return channelFuture;
    }
}
