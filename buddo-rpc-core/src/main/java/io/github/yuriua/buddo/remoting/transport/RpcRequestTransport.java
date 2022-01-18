package io.github.yuriua.buddo.remoting.transport;


import io.github.yuriua.buddo.extension.SPI;
import io.github.yuriua.buddo.remoting.transport.netty.client.dto.RpcRequest;

/**
 * send RpcRequest
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
