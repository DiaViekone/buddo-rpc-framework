package io.github.yuriua.buddo.loadbalance;

import io.github.yuriua.buddo.extension.SPI;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡接口定义
 */
@SPI
public interface LoadBalance {
    /**
     * 通过不同策略的负载均衡实现返回服务地址
     *
     * @param serviceAddresses Service address list
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest);
}
