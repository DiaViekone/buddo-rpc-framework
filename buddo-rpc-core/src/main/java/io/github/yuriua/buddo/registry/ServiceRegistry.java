package io.github.yuriua.buddo.registry;


import io.github.yuriua.buddo.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 发布服务（服务注册）
 */
@SPI
public interface ServiceRegistry {
    /**
     * 服务注册
     *
     * @param rpcServiceName    rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
