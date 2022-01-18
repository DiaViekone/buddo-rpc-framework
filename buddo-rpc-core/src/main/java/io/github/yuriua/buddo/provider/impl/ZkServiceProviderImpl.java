package io.github.yuriua.buddo.provider.impl;


import io.github.yuriua.buddo.config.BuddoConfig;
import io.github.yuriua.buddo.config.BuddoServerConfig;
import io.github.yuriua.buddo.config.RpcServiceConfig;
import io.github.yuriua.buddo.enums.RpcErrorMessageEnum;
import io.github.yuriua.buddo.exception.RpcException;
import io.github.yuriua.buddo.extension.ExtensionLoader;
import io.github.yuriua.buddo.provider.ServiceProvider;
import io.github.yuriua.buddo.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * zk实现
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * 已注册的服务map集合，客户端请求调用时，根据服务名从服务map集合中获取对应的服务，完成远程调用
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;

    /**
     * 已注册的服务，用于注册前判断，避免重复注册
     */
    private final Set<String> registeredService;

    /**
     * 服务管理器
     */
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        // 初始化存放服务的map
        serviceMap = new ConcurrentHashMap<>();
        //
        registeredService = ConcurrentHashMap.newKeySet();
        // 获取ServiceRegistry的zookeeper实现
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 添加服务
     * @param rpcServiceConfig rpc service related attributes
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        // 获取服务名
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            // 如果服务已注册就不添加到已注册set列表
            return;
        }
        // 将该服务名添加到已注册set列表中
        registeredService.add(rpcServiceName);
        // 保存到已注册服务map中
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 获取服务
     * @param rpcServiceName rpc service name
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }


    /**
     * 发布服务
     * @param rpcServiceConfig rpc service related attributes
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            // 获取当前的ip
            String host = InetAddress.getLocalHost().getHostAddress();
            // 添加到
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, BuddoServerConfig.getServerPort()));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}
