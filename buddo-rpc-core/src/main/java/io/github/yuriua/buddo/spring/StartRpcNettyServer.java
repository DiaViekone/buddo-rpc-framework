package io.github.yuriua.buddo.spring;

import io.github.yuriua.buddo.remoting.transport.netty.server.RpcNettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author lyx
 * @date 2022/1/19 4:34
 * Description: 初始化服务器
 */
@Slf4j
@Component
public class StartRpcNettyServer implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private RpcNettyServer rpcNettyServer;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        /*
         * 在web 项目中（spring mvc），系统会存在两个容器，一个是root application context ,
         * 另一个就是我们自己的 projectName-servlet  context（作为root application context的子容器）。
         * 这种情况下，就会造成onApplicationEvent方法被执行两次。为了避免上面提到的问题，
         * 我们可以只在root application context初始化完成后调用逻辑代码，其他的容器的初始化完成，则不做任何处理
         */
        if(event.getApplicationContext().getParent() == null)//root application context 没有parent，他就是老大.
        {
            //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
            rpcNettyServer.start();
            log.info("server initialization completed!");
        }
    }
}
