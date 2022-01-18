package io.github.yuriua.buddo.loadbalance;


import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;

import java.util.List;

/**
 * Abstract class for a load balancing policy
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        //只有一个直接返回
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        //多个执行
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);


}


