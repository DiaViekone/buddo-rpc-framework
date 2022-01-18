使用Netty开发的一款RPC框架，有详细中文注释，非常便于新手学习理解Netty使用

# buddo-rpc-framework

# 概述

buddo-rpc-framework是一款基于netty的RPC通信框架，虽然毫无卵用，但是对萌新学习Netty的使用和RPC框架基本原理有一定的帮助

# 架构

![](https://cdn.jsdelivr.net/gh/yuriua/images@master/application_upload_files/2022/01/18/dubbo-architecturee.png)

##### 节点角色说明

| 节点                | 角色说明                                   |
| ------------------- | ------------------------------------------ |
| `Provider`          | 暴露服务的服务提供方                       |
| `Consumer`          | 调用远程服务的服务消费方                   |
| `Registry`          | 服务注册与发现的注册中心                   |
| `Monitor`（已删除） | ~~统计服务的调用次数和调用时间的监控中心~~ |
| `Container`         | 服务运行容器                               |

删除了监控中心，更加轻量级

##### 调用关系说明

1. 服务容器负责启动，加载，运行服务提供者。
2. 服务提供者在启动时，向注册中心注册自己提供的服务。
3. 服务消费者在启动时，向注册中心订阅自己所需的服务。
4. 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
5. 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。
6. ~~服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。~~

# 使用方法

**可以参考示例工程**

流程：

1. 下载本项目并安装**buddo-core**

   编写接口工程、服务端工程、客户端工程，服务端工程和客户端工程依赖接口工程

   服务端和客户端依赖：

   ```xml
   <dependency>
       <groupId>io.github.yuriua.buddo</groupId>
       <artifactId>buddo-core</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

2. 服务端编写接口实现类，给服务成员打上`@RpcService`注解，使用`@RpcScan`扫描提供服务的包

3. 客户端使用`@RpcReference`注入代理实现，也需要使用`@RpcScan`扫描获取服务的包

4. 依次启动服务端（Spring加载IOC）、客户端（Spring加载IOC），客户端测试调用结果

   Spring加载IOC代码示例

   ~~~java
   // 扫描你发布/获取服务的包
   @RpcScan(basePackage = "com.example.service.impl")
   public class ServiceProviderApp {
       public static void main(String[] args) throws InterruptedException {
           // 加载所有的bean，buddo会自动完成初始化
           AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(ServiceProviderApp.class);
   
       }
   }
   ~~~

# 配置文件

~~~properties
# server
# 服务暴露端口
buddo.server.port=4869
# 服务端超时时间（客户端超过60s没有向服务端发送任何数据包括心跳包就会自动断开）
buddo.server.timeout=60

# client
# 心跳包发送间隔时间应该小于buddo.server.timeout，不设置默认是 buddo.server.timeout/2
buddo.client.heart-time=30

# 发布/获取服务的zookeeper地址
buddo.zookeeper.address=127.0.0.1:2181
~~~



