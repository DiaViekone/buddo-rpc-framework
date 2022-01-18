package io.github.yuriua.buddo.util;

/**
 * @author lyx
 * @date 2022/1/2 7:47
 * Description:
 */
public class RuntimeUtil {

    /**
     * 获取机器CPU核心数
     * @return
     */
    public static final int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }
}
