package com.tss.nettysocket.manager;

import com.tss.nettysocket.bean.Message;

/**
 * @author ：xiangjun.yang
 * @description：消息源
 */
public class MsgSource extends Observed {

    public void send(Message message){
        setChanged();
        notifyObservers(message);
    }
}
