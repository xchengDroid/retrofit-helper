package com.xcheng.retrofit.progress;

/**
 * 进度监听回调接口
 */
public interface ProgressListener {
    /**
     * @param progress      当前进度
     * @param contentLength 总长度
     * @param done          是否已经结束
     */
    void onProgress(long progress, long contentLength, boolean done);
}