package com.tss.nettysocket.manager;

import com.alibaba.fastjson.JSONObject;
import com.tss.nettysocket.bean.ComMode;
import com.tss.nettysocket.bean.SocketConn;
import com.tss.nettysocket.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ：xiangjun.yang
 * @description：socket连接管理单元
 */
public class ConnManager {
    private static final String PREFIX_MAP = "connectionMap:";

    private static Map<String,SocketConn> connMap = new ConcurrentHashMap<>();

    /**
     * 创建一个socket连接管理对象，存放到redis的连接队列和关联映射表中
     * @param sub
     * @param uid
     * @param channel
     * @throws Exception
     */
    public static void createConn(String sub, String uid, Channel channel) throws Exception {

        SocketConn socketConn = new SocketConn();
        socketConn.setChannelId(channel.id());
        socketConn.setChannel(channel);
        socketConn.setConnTime(LocalDateTime.now());
        socketConn.setSub(sub);

        //組播
        if (StringUtils.isEmpty(sub)) {
            socketConn.setComMode(ComMode.MULTICAST);
            socketConn.setUid(channel.id().asLongText());
            connMap.put(channel.id().asShortText(),socketConn);
            return;
        }

        //广播
        if (StringUtils.isEmpty(uid)) {
            socketConn.setComMode(ComMode.BROADCAST);
            socketConn.setUid(channel.id().asLongText());
            connMap.put(channel.id().asShortText(),socketConn);
            RedisUtil.getInstance().hset(PREFIX_MAP + sub,channel.id().asLongText(),channel.id().asShortText());
            return;
        }

        //单播
        socketConn.setComMode(ComMode.UNICAST);
        socketConn.setUid(uid);
        connMap.put(channel.id().asShortText(),socketConn);
        RedisUtil.getInstance().hset(PREFIX_MAP + sub,uid,channel.id().asShortText());
    }

    /**
     * 移除连接池里的socket连接
     * @param channelId
     * @throws Exception
     */
    public static void removeConn(ChannelId channelId) throws Exception {
        String shortText = channelId.asShortText();
        if (connMap.containsKey(shortText)) {
            SocketConn conn = connMap.get(shortText);

            connMap.remove(shortText);
            RedisUtil.getInstance().hdel(PREFIX_MAP + conn.getSub(),conn.getUid());
        }
    }

    /**
     * 获取单播的socket连接对象
     * @param sub
     * @param uid
     * @return
     * @throws Exception
     */
    public static SocketConn getConn(String sub, String uid) throws Exception {
        String channelId = RedisUtil.getInstance().hget(PREFIX_MAP + sub,uid);
        if (StringUtils.isEmpty(channelId)) {
            return null;
        }

        SocketConn conn = connMap.get(channelId);
        return conn;
    }

    /**
     * 获取广播的socket连接对象
     * @param sub
     * @return
     * @throws Exception
     */
    public static List<SocketConn> getConn(String sub) throws Exception {
        Collection<String> channelIds = RedisUtil.getInstance().hgetAll(PREFIX_MAP + sub).values();

        if (CollectionUtils.isEmpty(channelIds)) {
            return null;
        }

        List<SocketConn> conns = new ArrayList<>();
        for (String channelId : channelIds) {
            SocketConn conn = connMap.get(channelId);
            if (null != conn) {
                conns.add(conn);
            }
        }

        return conns;
    }

    /**
     * 获取全部的连接
     * @return
     */
    public static List<SocketConn> getConn(){
        return connMap.values().stream().collect(Collectors.toList());
    }

    public static SocketConn getConn(ChannelId channelId){
        return connMap.get(channelId.asShortText());
    }
}
