package com.tss.nettysocket.manager;

import com.tss.nettysocket.bean.Message;
import org.springframework.stereotype.Component;

/**
 * @author ：xiangjun.yang
 * @description：
 */
@Component("msgTest")
public class MsgProcess implements Observer {

    @Override
    public void update(Observed o, Message arg) {
        System.out.println(arg.toString());
    }
}
