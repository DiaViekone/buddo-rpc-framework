package io.github.yuriua.buddo.remoting.transport.netty.client;


import io.github.yuriua.buddo.remoting.message.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理请求
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        // 获取对应的future并从UNPROCESSED_RESPONSE_FUTURES中删除
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            // 填充结果并唤醒代理对象的阻塞（get()）方法
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
