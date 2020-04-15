package com.tss.nettysocket;

import com.tss.nettysocket.bean.Config;
import com.tss.nettysocket.handler.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettysocketApplication {
	private static final Logger log = LoggerFactory.getLogger(NettysocketApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(NettysocketApplication.class, args);
		try {
			Config.setHost("127.0.0.1");
			Config.setPort(6379);
			Config.setTimeout(1000);
			Config.setDatabase(9);
			Config.setMaxIdle(8);
			Config.setMaxWaitMillis(-1);
			Config.setPassword("683280");
			new NettyServer(2020).start();
		} catch (InterruptedException e) {
			log.error("netty socket service start fail: " + e.getMessage());
		}
	}

}
