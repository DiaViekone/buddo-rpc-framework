package com.example;

import com.example.controller.UserController;
import com.example.service.UserService;
import io.github.yuriua.buddo.annotation.RpcScan;
import io.github.yuriua.buddo.proxy.RpcClientProxy;
import io.github.yuriua.buddo.remoting.transport.netty.client.RpcNettyClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lyx
 * @date 2022/1/15 22:08
 * Description:
 */
@RpcScan(basePackage = "com.example.controller")
@ComponentScan("com.example.controller")
public class ServiceConsumerApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(ServiceConsumerApp.class);
        UserController userController = ioc.getBean(UserController.class);
        userController.userList();
//        RpcClientProxy rpcClientProxy = new RpcClientProxy(null,null);
//        UserService proxy = rpcClientProxy.getProxy(UserService.class);
    }
}
