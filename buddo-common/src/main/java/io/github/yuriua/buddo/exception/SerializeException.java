package io.github.yuriua.buddo.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lyx
 * @date 2022/1/2 1:04
 * Description:
 */
@Slf4j
public class SerializeException extends RuntimeException{
    public SerializeException(){

    }
    public SerializeException(String message) {
        super(message);
    }
}
