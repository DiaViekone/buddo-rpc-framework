package com.example;

import io.github.yuriua.buddo.annotation.RpcScan;
import io.github.yuriua.buddo.remoting.transport.netty.server.RpcNettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author lyx
 * @date 2022/1/15 21:31
 * Description:
 */
// 扫描你发布服务的包
@RpcScan(basePackage = "com.example.service.impl")
public class ServiceProviderApp {
    public static void main(String[] args) throws InterruptedException {

        // 加载所有的bean，buddo会自动完成初始化
        AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(ServiceProviderApp.class);

        //RpcNettyServer rpcNettyServer = ioc.getBean("rpcNettyServer", RpcNettyServer.class);

        //rpcNettyServer.start();
    }
}
