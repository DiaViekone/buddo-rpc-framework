package io.github.yuriua.buddo.remoting.test;

import io.github.yuriua.buddo.remoting.transport.netty.server.RpcNettyServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lyx
 * @date 2022/1/5 11:26
 * Description:
 */
@Slf4j
public class RpcNettyServerTest {
    public static void main(String[] args) {
        RpcNettyServer rpcNettyServer = new RpcNettyServer();
        rpcNettyServer.start();

    }
}
