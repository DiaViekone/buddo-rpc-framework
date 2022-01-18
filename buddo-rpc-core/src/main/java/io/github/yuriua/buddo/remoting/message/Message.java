package io.github.yuriua.buddo.remoting.message;

import io.github.yuriua.buddo.remoting.message.dto.AngelBeat;
import io.github.yuriua.buddo.remoting.message.dto.RpcMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Deprecated
public abstract class Message implements Serializable {

    /**
     * 根据消息类型字节，获得对应的消息 class
     * @param messageType 消息类型字节
     * @return 消息 class
     */
//    public static Class<? extends Message> getMessageClass(int messageType) {
//        return messageClasses.get(messageType);
//    }

    //消息序号
    private int sequenceId;

    //消息类型（获取消息类）
    private int messageType;

    public abstract int getMessageType();

    public static final int AngelBeat = 0;
    public static final int RpcMessage = 1;


    private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();

    static {
        messageClasses.put(AngelBeat, AngelBeat.class);
        //messageClasses.put(RpcMessage, RpcMessage.class);
    }

}
