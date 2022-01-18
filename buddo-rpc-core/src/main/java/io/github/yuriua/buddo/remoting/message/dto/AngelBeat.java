package io.github.yuriua.buddo.remoting.message.dto;

import io.github.yuriua.buddo.remoting.message.Message;

/**
 * @author lyx
 * @date 2022/1/2 10:06
 * Description:
 */
@Deprecated
public class AngelBeat extends Message {

    @Override
    public int getMessageType() {
        return AngelBeat;
    }
}
