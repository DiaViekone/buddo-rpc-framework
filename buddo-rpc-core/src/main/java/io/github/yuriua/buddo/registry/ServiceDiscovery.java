package io.github.yuriua.buddo.registry;



import io.github.yuriua.buddo.extension.SPI;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 通过服务名查找服务
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
