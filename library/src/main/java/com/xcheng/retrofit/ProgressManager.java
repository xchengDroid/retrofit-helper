package com.xcheng.retrofit;

import android.database.Observable;

import java.util.ArrayList;

/**
 * 创建时间：2018/7/31
 * 编写人： chengxin
 * 功能描述：管理进度
 */
public class ProgressManager extends Observable<ProgressManager.Listener> {
    private final ArrayList<Listener> mListeners =
            new ArrayList<>();


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
            mObservers.remove(index);
        }
    }


    /**
     * 进度监听回调接口
     */
    public static abstract class Listener {

        /**
         * 标记是下载还是上传
         */
        private final boolean downLoad;

        /**
         * 标记此Listener
         */
        private String tag;

        public Listener(String tag, boolean downLoad) {
            this.downLoad = downLoad;
        }

        /**
         * @param progress      当前进度
         * @param contentLength 总长度
         * @param done          是否已经结束
         */
        abstract void onProgress(long progress, long contentLength, boolean done);
    }
}
