package io.github.yuriua.buddo.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存和获取channel
 */
@Slf4j
public class ChannelProvider {

    /**
     * key:服务器地址 value:channel
     */
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 使用map缓存channel
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 存在且连接存活就直接返回
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                // 存在但连接已断开删除
                channelMap.remove(key);
            }
        }
        // 不存在直接返回null
        return null;
    }

    /**
     * 设置服务端地址与channel的缓存
     * @param inetSocketAddress
     * @param channel
     */
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    /**
     * 删除服务端地址与channel的缓存
     * @param inetSocketAddress
     */
    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }
}
