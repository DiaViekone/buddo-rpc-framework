package io.github.yuriua.buddo.provider;


import io.github.yuriua.buddo.config.RpcServiceConfig;
import io.github.yuriua.buddo.extension.SPI;

/**
 * 发布/获取服务
 */
@SPI
public interface ServiceProvider {

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
