package com.tss.nettysocket.manager;

import com.alibaba.fastjson.JSONObject;
import com.tss.nettysocket.bean.Message;
import com.tss.nettysocket.bean.MsgType;
import com.tss.nettysocket.bean.SocketConn;
import com.tss.nettysocket.handler.NettyServer;
import com.tss.nettysocket.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：xiangjun.yang
 * @description：消息处理单元
 */
@Component
public class MsgProcessor {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    @Autowired
    MsgManager msgManager;

    public void sendTo(String sub,Object msg) throws Exception {
        List<SocketConn> sockets = ConnManager.getConn(sub);

        if (CollectionUtils.isEmpty(sockets)) {
            return;
        }

        List<Channel> channels = sockets.stream().map(socketConn -> socketConn.getChannel()).collect(Collectors.toList());
        Message message = Message.build(MsgType.NOTIFY,sub,null,msg);
        String jsonMsg = JSONObject.toJSONString(message);
        msgManager.saveMsg(message);
        for (Channel channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(jsonMsg));
        }
        log.info("Send message to channel!");
    }

    public void sendTo(String sub,String uid,Object msg) throws Exception {
        SocketConn socketConn = ConnManager.getConn(sub,uid);

        if (null == socketConn) {
            return;
        }

        Message message = Message.build(MsgType.NOTIFY,sub,uid,msg);
        String jsonMsg = JSONObject.toJSONString(message);
        msgManager.saveMsg(message);
        socketConn.getChannel().writeAndFlush(new TextWebSocketFrame(jsonMsg));
        log.info("Send message to user!");
    }

    public void sendTo(Object msg) throws Exception {
        List<SocketConn> sockets = ConnManager.getConn();

        if (CollectionUtils.isEmpty(sockets)) {
            return;
        }

        List<Channel> channels = sockets.stream().map(socketConn -> socketConn.getChannel()).collect(Collectors.toList());
        Message message = Message.build(MsgType.NOTIFY,null,null,msg);
        String jsonMsg = JSONObject.toJSONString(message);
        for (Channel channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(jsonMsg));
        }
        log.info("Send message to all!");
    }
}
