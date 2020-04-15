package com.tss.nettysocket.manager;

import com.alibaba.fastjson.JSONObject;
import com.tss.nettysocket.bean.*;
import com.tss.nettysocket.util.RedisUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author ：xiangjun.yang
 * @description：消息管理单元
 */
@Component
public class MsgManager {
    private static boolean isPersistence = Config.isPersistence();
    private int maxWaitReply = Config.getMaxWaitReplySeconds();
    private static final String PREFIX = "message:";

    public void saveMsg(Message message) throws Exception {
        Objects.requireNonNull(message);
        Objects.requireNonNull(message.getSub());
        Objects.requireNonNull(message.getId());
        if (isPersistence){
            RedisUtil.getInstance().set(PREFIX + message.getId(), JSONObject.toJSONString(message));
            RedisUtil.getInstance().expire(PREFIX + message.getId(),maxWaitReply);
            RedisUtil.getInstance().hset(message.getSub(),message.getId(),JSONObject.toJSONString(message));
        }
    }

    public static void removeMsg(String id) throws Exception {
        Objects.requireNonNull(id);
        if (isPersistence) {
            Message message = JSONObject.parseObject(RedisUtil.getInstance().get(PREFIX + id),Message.class);
            if (null != message) {
                RedisUtil.getInstance().del(id);
                RedisUtil.getInstance().hdel(message.getSub(),id);
            }
        }
    }

    public static void dealHistory(ChannelHandlerContext ctx) throws Exception {
        ChannelId channelId = ctx.channel().id();
        SocketConn conn = ConnManager.getConn(channelId);
        Map<String,String> msgMap = RedisUtil.getInstance().hgetAll(conn.getSub());

        if (CollectionUtils.isEmpty(msgMap)) {
            return;
        }

        Collection<String> msgValues = msgMap.values();

        ComMode comMode = conn.getComMode();

        for (String msgValue : msgValues) {
            Message message = JSONObject.parseObject(msgValue,Message.class);
            if (ComMode.UNICAST.equals(comMode) && conn.getSub().equals(message.getSub()) && conn.getUid().equals(message.getUid())) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(msgValue));
                continue;
            }

            if (ComMode.BROADCAST.equals(comMode)) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(msgValue));
            }
        }
    }
}
