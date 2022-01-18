package io.github.yuriua.buddo.loadbalance.loadbalancer;

import io.github.yuriua.buddo.loadbalance.AbstractLoadBalance;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lyx
 * @date 2022/1/4 20:12
 * Description:
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {
    private static final ConcurrentHashMap<String,Integer> ROUND_ROBIN_NUM = new ConcurrentHashMap<>(0);
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Integer num = ROUND_ROBIN_NUM.get(serviceAddresses);
        if (null == num){
            //第一次来
            String rpcServiceName = rpcRequest.getRpcServiceName();
            ROUND_ROBIN_NUM.put(rpcServiceName,1);
            return serviceAddresses.get(0);
        }
        //第N次来
        int idx = num % serviceAddresses.size();
        return serviceAddresses.get(idx);
    }

}
