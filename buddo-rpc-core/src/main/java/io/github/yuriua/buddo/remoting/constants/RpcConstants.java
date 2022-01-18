package io.github.yuriua.buddo.remoting.constants;

import io.github.yuriua.buddo.config.BuddoConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rpc相关常量信息
 */
public class RpcConstants {


    /**
     * 魔数，验证消息
     */
    public static final byte[] MAGIC_NUMBER = {(byte) '8', (byte) '9', (byte) '6', (byte) '4'};
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //版本号
    public static final byte VERSION = 1;
    //头长度
    public static final byte TOTAL_LENGTH = 16;
    //请求号
    public static final byte REQUEST_TYPE = 1;
    //响应号
    public static final byte RESPONSE_TYPE = 2;
    //ping（心跳请求）
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong（心跳响应）
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
