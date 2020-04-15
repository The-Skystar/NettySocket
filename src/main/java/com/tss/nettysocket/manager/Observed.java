package com.tss.nettysocket.manager;

import com.tss.nettysocket.bean.Message;

import java.util.Vector;

/**
 * @author ：xiangjun.yang
 * @description：被观察者
 */
public class Observed {
    private boolean changed = false;
    private Vector<Observer> obs;

    /** Construct an Observed with zero Observers. */

    public Observed() {
        obs = new Vector<>();
    }

    public synchronized void addObserver(Observer o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    public synchronized void deleteObserver(Observer o) {
        obs.removeElement(o);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    public void notifyObservers(Message arg) {
        Object[] arrLocal;

        synchronized (this) {
            if (!changed) {
                return;
            }
            arrLocal = obs.toArray();
            clearChanged();
        }

        for (int i = arrLocal.length-1; i>=0; i--) {
            ((Observer)arrLocal[i]).update(this, arg);
        }
    }

    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return obs.size();
    }
}
