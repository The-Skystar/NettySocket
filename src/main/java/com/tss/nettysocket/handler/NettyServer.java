package com.tss.nettysocket.handler;

import com.tss.nettysocket.bean.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 */
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        // Boss线程：由这个线程池提供的线程是boss种类的，用于创建、连接、绑定socket， （有点像门卫）然后把这些socket传给worker线程池。
        // 在服务器端每个监听的socket都有一个boss线程来处理。在客户端，只有一个boss线程来处理所有的socket。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worker线程：Worker线程执行所有的异步I/O，即处理操作
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            // ServerBootstrap 启动NIO服务的辅助启动类,负责初始话netty服务器，并且开始监听端口的socket请求
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG,1024);
            sb.group(bossGroup, workGroup);
            // 设置非阻塞,用它来建立新accept的连接,用于构造serversocketchannel的工厂类
            sb.channel(NioServerSocketChannel.class);
            // ChildChannelHandler 对出入的数据进行的业务操作,其继承ChannelInitializer
            sb.childHandler(new ChildChannelHandler());
            sb.localAddress(port);
            ChannelFuture cf = sb.bind().sync();
            log.info("---->>The socket service is started and listening on the port " + cf.channel().localAddress());
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("---->>The socket service startup error! " + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel e) throws Exception {
            if (Config.isKeepAlive()) {
                // 设置30秒没有读到数据，则触发一个READER_IDLE事件。
                e.pipeline().addLast(new IdleStateHandler(Config.getReadIdleTimeSeconds(), Config.getWriteIdleTimeSeconds(), Config.getAllIdleTimeSeconds()));
            }
            // HttpServerCodec：将请求和应答消息解码为HTTP消息
            e.pipeline().addLast(new HttpServerCodec());
            // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
            e.pipeline().addLast(new HttpObjectAggregator(65536));
            // ChunkedWriteHandler：向客户端发送HTML5文件
            e.pipeline().addLast(new ChunkedWriteHandler());
            // 在管道中添加我们自己的接收数据实现方法
            e.pipeline().addLast(new WebSocketServerCompressionHandler());
            e.pipeline().addLast(new WebSocketHandler());
            e.pipeline().addLast(new WebSocketServerProtocolHandler("/ws",null,true,63356*10));

        }
    }
}
