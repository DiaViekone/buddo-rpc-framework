package io.github.yuriua.buddo.registry.zk;

import io.github.yuriua.buddo.registry.ServiceRegistry;
import io.github.yuriua.buddo.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author lyx
 * @date 2022/1/4 19:46
 * Description: zookeeper发布服务
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        //栗子: /buddo-rpc/io.github.yuriua.HelloServicetest1version1/192.168.122.1:9998
        String serverPath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,serverPath);
    }



}
