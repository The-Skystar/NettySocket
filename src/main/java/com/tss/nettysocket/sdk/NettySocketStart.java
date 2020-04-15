package com.tss.nettysocket.sdk;

import com.tss.nettysocket.bean.Config;
import com.tss.nettysocket.bean.RedisConfig;
import com.tss.nettysocket.handler.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 */
public class NettySocketStart {
    private static final Logger log = LoggerFactory.getLogger(NettySocketStart.class);

    public boolean start(RedisConfig redisConfig, int port){
        try {
            Config.setHost(redisConfig.getHost());
            Config.setPort(redisConfig.getPort());
            Config.setTimeout(redisConfig.getTimeout());
            Config.setDatabase(redisConfig.getDatabase());
            Config.setMaxIdle(redisConfig.getMaxIdle());
            Config.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
            Config.setPassword(redisConfig.getPassword());

            new NettyServer(port).start();
            return true;
        } catch (InterruptedException e) {
            log.error("netty socket service start fail: " + e.getMessage());
            return false;
        }
    }
}
