package com.tss.nettysocket.bean;

import com.tss.nettysocket.util.IDUtils;
import io.netty.channel.Channel;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ：xiangjun.yang
 * @description：socket通信传输的消息对象
 */
public class Message implements Serializable {
    private String id;
    private String sender;
    private String receiver;
    private MsgType msgType;
    private String timeStamp;
    private String sub;
    private String uid;
    private Object data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Message() {
    }

    public Message(MsgType msgType, String sub, String uid, Object data) {
        this.id = IDUtils.getUUID();
        this.timeStamp = LocalDateTime.now().toString();
        this.msgType = msgType;
        this.sub = sub;
        this.uid = uid;
        this.data = data;
    }

    public Message(String sender, String receiver, MsgType msgType, String timeStamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.msgType = msgType;
        this.timeStamp = timeStamp;
    }

    public static Message build(Channel channel) {
        return new Message(channel.id().asShortText(),channel.id().asShortText(), MsgType.HEARTBEAT,LocalDateTime.now().toString());
    }

    public static Message build(MsgType msgType, String sub, String uid, Object data){
        return new Message(msgType,sub,uid,data);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", msgType=" + msgType +
                ", timeStamp='" + timeStamp + '\'' +
                ", sub='" + sub + '\'' +
                ", uid='" + uid + '\'' +
                ", data=" + data +
                '}';
    }
}
