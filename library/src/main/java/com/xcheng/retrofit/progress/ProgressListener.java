package com.xcheng.retrofit.progress;

import android.support.annotation.WorkerThread;

import okhttp3.Request;

/**
 * 进度监听回调接口
 */
public interface ProgressListener {

    /**
     * @param request       此请求的request
     * @param progress      当前进度
     * @param contentLength 总长度
     * @param done          是否已经结束
     */
    @WorkerThread
    void onUpload(Request request, long progress, long contentLength, boolean done);

    @WorkerThread
    void onDownload(Request request, long progress, long contentLength, boolean done);
}