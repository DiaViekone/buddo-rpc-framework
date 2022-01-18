package io.github.yuriua.buddo.remoting.transport.netty.client;

import io.github.yuriua.buddo.enums.CompressTypeEnum;
import io.github.yuriua.buddo.enums.SerializationTypeEnum;
import io.github.yuriua.buddo.extension.ExtensionLoader;
import io.github.yuriua.buddo.registry.ServiceDiscovery;
import io.github.yuriua.buddo.remoting.constants.RpcConstants;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import io.github.yuriua.buddo.remoting.transport.RpcRequestTransport;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;
import io.github.yuriua.buddo.remoting.transport.netty.codec.RpcMessageDecoder;
import io.github.yuriua.buddo.remoting.transport.netty.codec.RpcMessageEncoder;
import io.github.yuriua.buddo.remoting.transport.netty.handler.NettyRpcClientHandler;
import io.github.yuriua.buddo.util.SingletonFactory;
import io.github.yuriua.buddo.config.BuddoClientConfig;
import io.github.yuriua.buddo.config.BuddoConfig;
import io.github.yuriua.buddo.remoting.transport.netty.codec.MessageCodec;
import io.github.yuriua.buddo.remoting.transport.netty.handler.AngelBeatsHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static io.github.yuriua.buddo.util.SingletonFactory.getInstance;

/**
 * @author lyx
 * @date 2022/1/2 10:24
 * Description:
 */
@Slf4j
public class RpcNettyClient implements RpcRequestTransport {
    //== netty handler ==//
    /**
     * netty打印出入站消息，会根据加入ChannelPipeline的顺序打印数据（栗子：放到解码器前面，入站打印的就是未解码数据）
     */
    private static final LoggingHandler LOGGING_HANDLER = getInstance(LoggingHandler.class,LogLevel.DEBUG);
    /**
     * 处理心跳包
     */
    private static final AngelBeatsHandler ANGEL_BEATS_HANDLER = getInstance(AngelBeatsHandler.class);
    /**
     * 自定义的编解码器
     */
    private static final MessageCodec MESSAGE_CODEC = getInstance(MessageCodec.class);
    /**
     * 处理服务调用方结果的处理器
     */
    private static final NettyRpcClientHandler NETTY_RPC_CLIENT_HANDLER = getInstance(NettyRpcClientHandler.class);
    /**
     * 解码器
     */
    private static final RpcMessageEncoder RPC_MESSAGE_ENCODER = new RpcMessageEncoder();
    /**
     * netty组件，NIO事件循环组
     */
    private final NioEventLoopGroup eventLoopGroup;
    /**
     * netty组件
     */
    private final Bootstrap bootstrap;
    /**
     * 服务发现
     */
    private final ServiceDiscovery serviceDiscovery;
    /**
     * channel提供者
     */
    private final ChannelProvider channelProvider;
    /**
     * 保存异步获取的调用结果
     */
    private final UnprocessedRequests unprocessedRequests;

    public RpcNettyClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 处理器管道对象，用于管理处理器
                        ChannelPipeline p = ch.pipeline();
                        // 每隔15秒向服务端发送心跳包防止长时间未请求被断开
                        p.addLast(new IdleStateHandler(0, BuddoClientConfig.getHeartSendTime(), 0, BuddoConfig.getDefaultTimeUnit()));
                        // 日志
                        p.addLast(LOGGING_HANDLER);
//                        // 参数1：最大帧长度 参数2：长度字节偏移量 参数3：长度字长 参数4：正文偏移量（长度字长后面的非正文数据） 参数5：从头剥离几个字长
//                        p.addLast(new LengthFieldBasedFrameDecoder(1024,12,4,0,16));
                        // 编解码器
//                        p.addLast(MESSAGE_CODEC);
                        // 编码器
                        p.addLast(RPC_MESSAGE_ENCODER);
                        // 解码器(实现了LengthFieldBasedFrameDecoder，如果包过大会保存多余部分，因此不能使用单例)
                        p.addLast(new RpcMessageDecoder());
                        // RPC客户端处理器
                        p.addLast(NETTY_RPC_CLIENT_HANDLER);
                    }
                });
        // 通过SPI机制获取ServiceDiscovery实现类
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        // channel提供者
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        // 异步结果处理器
        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 发起连接并获得channel
     *
     * @param inetSocketAddress server address
     * @return the channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // 新建一个异步任务处理对象
        //CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 使用Promise
        DefaultPromise<Channel> promise = new DefaultPromise<>(eventLoopGroup.next());
        // nio线程发起连接
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("connected: {}", inetSocketAddress.toString());
                //completableFuture.complete(future.channel());
                promise.setSuccess(future.channel());
            } else {
                promise.setFailure(new IllegalStateException());
            }
        });
        // 当前线程等待netty的nio线程连接成功结果 TODO: 这里不能用get，连接失败会一直阻塞，改为多次尝试
        ;
        if (promise.sync().isSuccess()){
            return promise.get();
        }
        throw promise.cause();
    }



    /**
     * 发起调用（核心方法）
     * @param rpcRequest message body
     * @return
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 保存异步处理结果
        CompletableFuture resultFuture = new CompletableFuture();
        // 获取服务器地址
        InetSocketAddress serviceAddress = serviceDiscovery.lookupService(rpcRequest);
        // 获取channel
        Channel channel = getChannel(serviceAddress);
        if (channel.isActive()){
            // 保存到 unprocessed request
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            // 构建请求
            RpcMessage rpcMessage = RpcMessage.builder()
                    // 设置请求调用的接口信息
                    .data(rpcRequest)
                    // 设置序列化类型
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    // 设置压缩算法 TODO: 当前默认GZIP
                    .compress(CompressTypeEnum.GZIP.getCode())
                    // 设置消息类型: 请求
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()){
                        log.info("requesting server invocation: {}",rpcRequest.toString());
                    } else {
                        // 关闭连接
                        future.channel().close();
                        // 设置future对象为失败状态
                        resultFuture.completeExceptionally(future.cause());
                        log.error("request server invocation error: {}",future.cause());
                    }
                }
            });

        }
        return resultFuture;
    }

    /**
     * 通过ip:port获取channel
     * @param inetSocketAddress
     * @return
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        // 先从缓存获取
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            // 没有发起连接请求
            channel = doConnect(inetSocketAddress);
            // 保存到缓存下次使用
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 关闭与服务端的连接
     */
    public void close() {
        // 完美的关闭连接
        eventLoopGroup.shutdownGracefully();
    }
}
