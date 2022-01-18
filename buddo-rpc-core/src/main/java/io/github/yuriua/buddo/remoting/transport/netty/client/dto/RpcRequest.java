package io.github.yuriua.buddo.remoting.transport.netty.client.dto;

import lombok.*;

import java.io.Serializable;

/**
 * RPC请求体，封装了调用的方法、参数等数据
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1989060401955061599L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }


}
