package com.tss.nettysocket.manager;

import com.tss.nettysocket.bean.Message;

/**
 * @author ：xiangjun.yang
 * @description：观察者
 */
public interface Observer {

    void update(Observed o, Message arg);
}
