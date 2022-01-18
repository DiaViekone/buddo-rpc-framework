package io.github.yuriua.buddo.registry.zk.util;

import io.github.yuriua.buddo.util.PropertiesUtil;
import io.github.yuriua.buddo.config.BuddoServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lyx
 * @date 2022/1/3 21:47
 * Description: zk客户端工具
 */
@Slf4j
public class CuratorUtils {


    public static void main(String[] args) {
        CuratorFramework zkClient = getZkClient();
        createPersistentNode(zkClient,"/yuriua");

    }
    //注册中心地址
    public static final String ZK_REGISTER_ROOT_PATH = "/buddo-rpc";
    //key:服务名 value:可用实现
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //zk客户端
    private static CuratorFramework zkClient;
    //默认地址
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    //存储所有创建的节点。每创建一个节点父节点都会保存到该集合
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    public CuratorUtils() {
    }

    /**
     * 创建持久节点。 与临时节点不同，客户端断开连接时不会删除持久节点
     * @param zkClient CuratorFramework实例
     * @param path 节点路径 例子: /yuriua
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("zookeeper node already exists: {}",path);
            }else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("zookeeper node created: {}",path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取节点下的子节点 栗子：/buddo-rpc/io.github.yuriua.service.impl.UserServiceImplversion1
     * @param zkClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        //返回的服务可能有多个（集群）
        List<String> resultServers = null;
        String serverPath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            //                     获取某一节点下所有子节点
            resultServers = zkClient.getChildren().forPath(serverPath);
        } catch (Exception e) {
            log.error("get zookeeper children node fail: {}",serverPath);
        }
        return resultServers;
    }


    /**
     * 清空节点
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        //stream().parallel(): 并行流（分割流数据多核处理）
        REGISTERED_PATH_SET.stream().parallel().forEach(p->{
            try {
                if (p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("remove zookeeper node fail: {}",p);
            }
        });
        //打印提示信息，不代表节点一定全部删除成功
        log.info("remove zookeeper node successful: "+REGISTERED_PATH_SET.toString());
    }



    /**
     * 获取zkClient
     */
    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        //TODO: 重构为动态获取
        Properties properties = PropertiesUtil.loadProperties("buddo.properties");
        String zkAddress = BuddoServerConfig.getZookeeperAddress();
        //获取zk连接重试策略，3次连接不上就增加再次重试之前的休眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        //构建zkClient
        zkClient = CuratorFrameworkFactory.builder()
                //连接地址
                .connectString(zkAddress)
                //重试策略
                .retryPolicy(retryPolicy)
                .build();
        //真正的开始连接
        zkClient.start();
        try {
            zkClient.blockUntilConnected(5, TimeUnit.SECONDS);
            log.info("初始化CuratorFramework成功，已连接到zookeeper[{}]",zkAddress);
        } catch (InterruptedException e) {
            log.error("连接zookeeper超时[{}]",zkAddress);
            //Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * 监听节点。例如：/buddo/io.github.yuriua.xxx.UserService 就是监听*UserService这个节点下的子节点，不包括孙子节点
     * @param zkClient
     * @param rpcServiceName
     */
    private static void registerWatcher( CuratorFramework zkClient,String rpcServiceName) throws Exception {
        String serverPath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        //                                                                              参数：true代表缓存数据到本地
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, rpcServiceName, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                List<String> serviceAddresses = client.getChildren().forPath(serverPath);
                SERVICE_ADDRESS_MAP.put(serverPath,serviceAddresses);
            }
        });
        pathChildrenCache.start();
    }



}
