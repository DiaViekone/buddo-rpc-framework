package io.github.yuriua.buddo.config;

import java.net.InetSocketAddress;

/**
 * @author lyx
 * @date 2022/1/3 22:21
 * Description: 客户端配置
 */
public class BuddoClientConfig extends BuddoConfig{

    private static final int BUDDO_CLIENT_HEART_TIME = BuddoServerConfig.getServerTimeout() / 2;
    /**
     * 默认拉取的服务ip地址
     */
    private static final InetSocketAddress DEFAULT_REMOTE_ADDRESS = new InetSocketAddress("127.0.0.1",8964);

    /**
     * 客户端发送心跳包间隔（发送心跳包间隔必须小于服务端超时时间）
     */
    public static int getHeartSendTime(){
        return Integer.parseInt(properties.getProperty("buddo.client.heart-time",String.valueOf(BUDDO_CLIENT_HEART_TIME)));
    }

    /**
     * 获取拉取服务的ip地址
     */
//    public static InetSocketAddress getRemoteAddress(){
//        InetSocketAddress remoteAddress;
//        String property = properties.getProperty("buddo.remote.address");
//        if (property != null && URL_PATTERN.matcher(property).find()){
//            String[] hostAndPort = property.split(":");
//            remoteAddress = new InetSocketAddress(hostAndPort[0],Integer.parseInt(hostAndPort[1]));
//        } else {
//            remoteAddress = DEFAULT_REMOTE_ADDRESS;
//        }
//        return remoteAddress;
//    }
}
