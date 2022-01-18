package io.github.yuriua.buddo.registry.zk;

import io.github.yuriua.buddo.enums.RpcErrorMessageEnum;
import io.github.yuriua.buddo.exception.RpcException;
import io.github.yuriua.buddo.extension.ExtensionLoader;
import io.github.yuriua.buddo.loadbalance.LoadBalance;
import io.github.yuriua.buddo.registry.ServiceDiscovery;
import io.github.yuriua.buddo.registry.zk.util.CuratorUtils;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author lyx
 * @date 2022/1/4 19:42
 * Description: zookeeper服务发现实现
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        // SPI扩展注入 TODO
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    /**
     * 获取服务对应的ip:port
     * @param rpcRequest rpc service pojo
     * @return
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //栗子: 获取 /buddo-rpc/com.example.service.UserServicebuddo-test1.0 下所有保存的数据（ip地址）
        List<String> serverUrlList= CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serverUrlList == null || serverUrlList.size() == 0){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        //负载均衡
        String host = loadBalance.selectServiceAddress(serverUrlList, rpcRequest);
        String[] socketAddressArray = host.split(":");
        String ip = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(ip,port);
    }
}
