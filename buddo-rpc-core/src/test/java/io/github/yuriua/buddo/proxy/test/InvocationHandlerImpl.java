package io.github.yuriua.buddo.proxy.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author lyx
 * @date 2022/1/5 11:55
 * Description:
 */
public class InvocationHandlerImpl implements InvocationHandler {

    private Object object;

    public InvocationHandlerImpl(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        method.setAccessible(true);
        Object invoke = method.invoke(object, args);
        return invoke;
    }
}
