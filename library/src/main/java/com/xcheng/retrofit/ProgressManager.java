package com.xcheng.retrofit;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 创建时间：2018/7/31
 * 编写人： chengxin
 * 功能描述：管理进度监听类
 */
public class ProgressManager {

    private volatile static ProgressManager instance;

    private final ArrayList<Listener> listeners =
            new ArrayList<>();
    @Nullable
    private volatile Executor callbackExecutor;

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

    public void with(@Nullable Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Nullable
    public Executor callbackExecutor() {
        return callbackExecutor;
    }

    public void registerListener(Listener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " is already registered.");
            }
            listeners.add(listener);
        }
    }

    public void unregisterListener(Listener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            int index = listeners.indexOf(listener);
            if (index == -1) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            listeners.remove(index);
        }
    }

    public void unregisterListener(String tag) {
        Utils.checkNotNull(tag, "tag==null");
        synchronized (listeners) {
            for (int index = 0; index < listeners.size(); index++) {
                Listener listener = listeners.get(index);
                if (listener.tag.equals(tag)) {
                    listeners.remove(index);
                    index--;
                }
            }
        }
    }


    /**
     * 获取对应的Listener,如果不存在返回size==0的ArrayList
     *
     * @param tag      对应的tag
     * @param download true代表下载，false代表上传
     * @return
     */
    public List<Listener> getListeners(String tag, boolean download) {
        Utils.checkNotNull(tag, "tag==null");
        List<Listener> listeners = new ArrayList<>();
        synchronized (this.listeners) {
            for (int index = 0; index < this.listeners.size(); index++) {
                Listener listener = this.listeners.get(index);
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

    /**
     * 进度监听回调接口
     */
    public static abstract class Listener {
        /**
         * 标记是下载还是上传
         */
        public final boolean download;
        /**
         * 标记此Listener
         */
        public final String tag;

        public Listener(String tag, boolean download) {
            Utils.checkNotNull(tag, "tag==null");
            this.tag = tag;
            this.download = download;
        }

        /**
         * @param progress      当前进度
         * @param contentLength 总长度
         * @param done          是否已经结束
         */
        protected abstract void onProgress(long progress, long contentLength, boolean done);
    }
}
