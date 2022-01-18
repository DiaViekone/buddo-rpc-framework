package io.github.yuriua.buddo.remoting.transport.netty.handler;


import io.github.yuriua.buddo.exception.RpcException;
import io.github.yuriua.buddo.provider.ServiceProvider;
import io.github.yuriua.buddo.provider.impl.ZkServiceProviderImpl;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;
import io.github.yuriua.buddo.util.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author lyx
 * @date 2022/1/4 21:22
 * Description: Rpc请求处理器
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     * 处理rpc调用请求：调用对应的方法，然后返回方法结果
     */
    public Object handle(RpcRequest rpcRequest) {
        // 获取服务实例
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        // 执行
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * get method execution results
     *
     * @param rpcRequest client request 客户端请求数据
     * @param service    service object 对应的服务
     * @return the result of the target method execution 执行完毕的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            //反射获取方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            //执行调用
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        //返回结果
        return result;
    }
}
