package com.xcheng.retrofit.progress;

import android.support.annotation.GuardedBy;

import com.xcheng.retrofit.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2018/7/31
 * 编写人： chengxin
 * 功能描述：管理进度监听类
 */
public class ProgressManager {

    private volatile static ProgressManager instance;

    @GuardedBy("listeners")
    private final ArrayList<ProgressListener> listeners =
            new ArrayList<>();

    private ProgressManager() {
    }

    public static ProgressManager getInstance() {
        if (instance == null) {
            synchronized (ProgressManager.class) {
                if (instance == null) {
                    instance = new ProgressManager();
                }
            }
        }
        return instance;
    }

    public void registerListener(ProgressListener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " is already registered.");
            }
            listeners.add(listener);
        }
    }

    public void unregisterListener(ProgressListener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            int index = listeners.indexOf(listener);
            if (index == -1) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            listeners.remove(index);
        }
    }

    public void unregisterListeners(String tag) {
        Utils.checkNotNull(tag, "tag==null");
        synchronized (listeners) {
            for (int index = 0; index < listeners.size(); index++) {
                ProgressListener listener = listeners.get(index);
                if (listener.tag.equals(tag)) {
                    listeners.remove(index);
                    index--;
                }
            }
        }
    }


    /**
     * @param tag      对应的tag
     * @param download true代表下载，false代表上传
     * @return 获取对应的Listener, 如果不存在返回size==0的ArrayList
     */
    public List<ProgressListener> getListeners(String tag, boolean download) {
        Utils.checkNotNull(tag, "tag==null");
        List<ProgressListener> listeners = new ArrayList<>();
        synchronized (this.listeners) {
            for (int index = 0; index < this.listeners.size(); index++) {
                ProgressListener listener = this.listeners.get(index);
                if (listener.tag.equals(tag)
                        && listener.download == download) {
                    listeners.add(listener);
                }
            }
        }
        return listeners;
    }

    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized (listeners) {
            listeners.clear();
        }
    }
}
