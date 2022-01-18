package io.github.yuriua.buddo.extension;

/**
 * 持有者
 * @param <T>
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
