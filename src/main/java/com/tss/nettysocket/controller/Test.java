package com.tss.nettysocket.controller;

import com.tss.nettysocket.bean.Message;
import com.tss.nettysocket.bean.SocketConn;
import com.tss.nettysocket.manager.MsgProcessor;
import com.tss.nettysocket.sdk.NettySocketMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ：xiangjun.yang
 * @description：
 */
@RestController
public class Test {


    @RequestMapping("/test")
    public void test() throws Exception {
        NettySocketMsg socketMsg = new NettySocketMsg();
        SocketConn conn = new SocketConn();
        conn.setUid("1321831");
        conn.setSub("cnxjcxhusdaisda");
        socketMsg.sendTo("test",conn);
        socketMsg.sendTo("giao","123",conn);
    }
}
