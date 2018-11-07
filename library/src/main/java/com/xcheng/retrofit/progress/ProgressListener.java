package com.xcheng.retrofit.progress;

import android.support.annotation.WorkerThread;

/**
 * 进度监听回调接口
 */
public interface ProgressListener {

    /**
     * @param tag           标记此监听
     * @param progress      当前进度
     * @param contentLength 总长度
     * @param done          是否已经结束
     */
    @WorkerThread
    void onUpload(String tag, long progress, long contentLength, boolean done);

    @WorkerThread
    void onDownload(String tag, long progress, long contentLength, boolean done);
}