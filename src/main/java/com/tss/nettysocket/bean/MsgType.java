package com.tss.nettysocket.bean;

/**
 * @author ：xiangjun.yang
 * @description：消息类型
 */
public enum MsgType {
    //心跳消息
    HEARTBEAT,
    //客户端发送的消息
    SEND,
    //服务端推送的消息
    NOTIFY,
    //客户端应答的消息
    REPLY,
    //客户端保活
    ASK,
    //需要转发的消息
    FORWARD
}
