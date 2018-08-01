package com.xcheng.retrofit;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间：2018/7/31
 * 编写人： chengxin
 * 功能描述：管理进度监听类
 */
public class ProgressManager {
    private final ArrayList<Listener> mListeners =
            new ArrayList<>();

    private volatile static ProgressManager instance;

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

    public void registerListener(Listener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (mListeners) {
            if (mListeners.contains(listener)) {
                throw new IllegalStateException("Listener " + listener + " is already registered.");
            }
            mListeners.add(listener);
        }
    }

    public void unregisterListener(Listener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (mListeners) {
            int index = mListeners.indexOf(listener);
            if (index == -1) {
                throw new IllegalStateException("Listener " + listener + " was not registered.");
            }
            mListeners.remove(index);
        }
    }

    public void unregisterListener(String tag) {
        Utils.checkNotNull(tag, "tag==null");
        synchronized (mListeners) {
            for (int index = 0; index < mListeners.size(); index++) {
                Listener listener = mListeners.get(index);
                if (listener.tag.equals(tag)) {
                    mListeners.remove(index);
                    index--;
                }
            }
        }
    }

    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized (mListeners) {
            mListeners.clear();
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
        synchronized (mListeners) {
            for (int index = 0; index < mListeners.size(); index++) {
                Listener listener = mListeners.get(index);
                if (listener.tag.equals(tag)
                        && listener.download == download) {
                    listeners.add(listener);
                }
            }
        }
        return listeners;
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
        abstract void onProgress(long progress, long contentLength, boolean done);
    }
}
