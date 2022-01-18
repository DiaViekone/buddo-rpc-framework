package io.github.yuriua.buddo.proxy.test;

import java.lang.reflect.Method;

/**
 * @author lyx
 * @date 2022/1/5 19:57
 * Description:
 */
class Test {
    public static void main(String[] args) throws NoSuchMethodException {
        Class<Object> c = Object.class;
        Method finalize = c.getDeclaredMethod("wait");
        System.out.println(finalize.getDeclaringClass().getName());
    }
}
