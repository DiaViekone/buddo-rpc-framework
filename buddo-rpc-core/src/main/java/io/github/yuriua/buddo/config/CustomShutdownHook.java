package io.github.yuriua.buddo.config;

import io.github.yuriua.buddo.registry.zk.util.CuratorUtils;
import io.github.yuriua.buddo.util.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author lyx
 * @date 2022/1/2 7:27
 * Description: 服务器关闭时清理
 */
@Slf4j
public class CustomShutdownHook {

    public static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){

        return CUSTOM_SHUTDOWN_HOOK;
    }
    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), BuddoServerConfig.getServerPort());
                // 删除节点
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
