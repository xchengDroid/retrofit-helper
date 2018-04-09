package com.xcheng.retrofit.progress;

/**
 * 进度监听回调接口
 */
public interface ProgressListener {
    void onProgress(long progress, long contentLength, boolean done);
}