package io.github.yuriua.buddo.config;

import java.util.regex.Pattern;

/**
 * @author lyx
 * @date 2022/1/3 22:20
 * Description: 服务端配置
 */
public class BuddoServerConfig extends BuddoConfig{

    /**
     * 作为服务提供方发布者发布时对外暴露的端口号
     */
    private static final int DEFAULT_SERVER_PORT = 8964;
    /**
     * 默认超时时间，当客户端在30s子类没有发送任何数据（包括心跳包），即表面该客户端已断开连接，服务端关闭该通道节省服务端连接资源
     */
    private static final int BUDDO_SERVER_TIMEOUT = 30;
    /**
     * default server manager： zookeeper address
     */
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    /**
     * 获取服务端端口号，客户端应该与服务端端口号配置一致
     * @return
     */
    public static int getServerPort(){
        return Integer.parseInt(properties.getProperty("buddo.server.port",String.valueOf(DEFAULT_SERVER_PORT)));
    }


    /**
     * 获取服务器超时时间（服务器超过该时间没有收到客户端请求调用或心跳包就会端口）
     */
    public static int getServerTimeout(){
        return Integer.parseInt(properties.getProperty("buddo.server.timeout", String.valueOf(BUDDO_SERVER_TIMEOUT)));
    }

    /**
     * 获取zookeeper地址
     */
    public static String getZookeeperAddress(){
        String zkAddr = properties.getProperty("buddo.zookeeper.address",DEFAULT_ZOOKEEPER_ADDRESS);
        if (URL_PATTERN.matcher(zkAddr).find()){
            return zkAddr;
        } else {
            return DEFAULT_ZOOKEEPER_ADDRESS;
        }
    }


}
