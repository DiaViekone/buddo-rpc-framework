package io.github.yuriua.buddo.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例工厂
 */
public final class SingletonFactory {

    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }

    public static <T> T getInstance(Class<T> c,Object... obj) {
        return getInstance(c,false,obj);
    }

    /**
     *
     * @param c 类文件
     * @param obj 构造参数列表
     * @param <T>
     * @param setAccessible 忽略修饰符(private)强制实例化
     * @return
     */
    public static <T> T getInstance(Class<T> c,boolean setAccessible,Object... obj) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    Class[] clz = new Class[obj.length];
                    for (int i = 0; i < clz.length; i++) {
                        clz[i] = obj[i].getClass();
                    }
                    Constructor<T> declaredConstructor = c.getDeclaredConstructor(clz);
                    //直接可以使用私有构造创建对象
                    declaredConstructor.setAccessible(setAccessible);
                    return declaredConstructor.newInstance(obj);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }
}
