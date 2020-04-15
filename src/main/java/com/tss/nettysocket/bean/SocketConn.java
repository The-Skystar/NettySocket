package com.tss.nettysocket.bean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author ：xiangjun.yang
 * @description：socket连接管理对象
 */
public class SocketConn implements Serializable {
    private String sub;
    private String uid;
    private ChannelId channelId;
    private Channel channel;
    private ComMode comMode;
    private LocalDateTime connTime;

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

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ComMode getComMode() {
        return comMode;
    }

    public void setComMode(ComMode comMode) {
        this.comMode = comMode;
    }

    public LocalDateTime getConnTime() {
        return connTime;
    }

    public void setConnTime(LocalDateTime connTime) {
        this.connTime = connTime;
    }

    @Override
    public String toString() {
        return "SocketConn{" +
                "sub='" + sub + '\'' +
                ", uid='" + uid + '\'' +
                ", channelId=" + channelId +
                ", channel=" + channel +
                ", comMode=" + comMode +
                ", connTime=" + connTime +
                '}';
    }
}
