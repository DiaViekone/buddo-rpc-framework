package io.github.yuriua.buddo.proxy.test;

import java.lang.reflect.Proxy;

/**
 * @author lyx
 * @date 2022/1/5 11:53
 * Description:
 */
public class ProxyTest {
    public static void main(String[] args) {

        TargetInterface tf = (TargetInterface) Proxy.newProxyInstance(Target.class.getClassLoader(),Target.class.getInterfaces(),new InvocationHandlerImpl(new Target()));

        tf.m1();
    }
}
