package io.github.yuriua.buddo.proxy.test;

/**
 * @author lyx
 * @date 2022/1/5 11:54
 * Description:
 */
public class Target implements TargetInterface{
    @Override
    public void m1() {
        System.out.println("m1");
    }

    @Override
    public String m2() {
        System.out.println("m2");
        return "m2 result";
    }
}
