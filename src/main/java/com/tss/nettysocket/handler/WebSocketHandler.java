package com.tss.nettysocket.handler;

import com.alibaba.fastjson.JSONObject;
import com.tss.nettysocket.bean.Message;
import com.tss.nettysocket.bean.MsgType;
import com.tss.nettysocket.manager.*;
import com.tss.nettysocket.util.ApplicationContextUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * 客户端与服务端创建连接的时候调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("---->>Establish a WebSocket connection, channelId is [" + ctx.channel().id() + "]");
    }

    /**
     * 客户端与服务端断开连接的时候调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("---->>WebSocket connection with channelId [" + ctx.channel().id() + "] has been closed");
        ConnManager.removeConn(ctx.channel().id());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        MsgManager.dealHistory(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理客户端向服务端发起http握手请求的业务
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            if (!req.getDecoderResult().isSuccess() || !("websocket".equals(req.headers().get("Upgrade")))) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                log.warn("---->>It is not a webSocket connection!");
                return;
            }

            String url = req.uri();
            Map<String,String> params = getUrlParams(url);
            if (url.contains("?")) {
                String newUri = url.substring(0,url.indexOf('?'));
                req.setUri(newUri);
            }

            ConnManager.createConn(params.get("sub"),params.get("uid"),ctx.channel());
        }
        super.channelRead(ctx,msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        log.info(textWebSocketFrame.text());
        Message message = JSONObject.parseObject(textWebSocketFrame.text(),Message.class);
        if (MsgType.SEND.equals(message.getMsgType())) {
            MsgSource source = new MsgSource();
            source.addObserver((Observer) ApplicationContextUtils.getBean("msgTest"));
            source.send(message);
        }

        if (MsgType.REPLY.equals(message.getMsgType())) {
            String id = message.getId();

            if (!StringUtils.isEmpty(id)) {
                MsgManager.removeMsg(id);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("---->>Read idle,webSocket connection with channelId [" + ctx.channel().id() + "] has been closed");
                ConnManager.removeConn(ctx.channel().id());
                ctx.disconnect();
            }

            if (event.state() == IdleState.WRITER_IDLE) {
                log.info("---->>send heartbeat packet!");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(Message.build(ctx.channel()))));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("---->>The webSocket connection with channelId [" + ctx.channel().id() + "] is abnormal!");
        ConnManager.removeConn(ctx.channel().id());
        cause.printStackTrace();
        ctx.close();
    }

    private Map<String,String> getUrlParams(String url) {
        Map<String,String> map = new HashMap<>();
        url = url.replace('?',';');
        if (!url.contains(";")) {
            return map;
        }

        if (url.split(";").length > 0) {
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr) {
                String[] attr = s.split("=");
                if (2 == attr.length) {
                    String key = attr[0];
                    String value = attr[1];
                    map.put(key,value);
                }
            }
            return map;
        }
        return map;
    }

    /**
     * 服务端向客户端响应消息
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                  DefaultFullHttpResponse res){
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
