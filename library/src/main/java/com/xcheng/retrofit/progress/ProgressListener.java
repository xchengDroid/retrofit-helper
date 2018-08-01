package com.xcheng.retrofit.progress;

import com.xcheng.retrofit.Utils;

/**
 * 进度监听回调接口
 */
public abstract class ProgressListener {
    /**
     * 标记是下载还是上传
     */
    public final boolean download;
    /**
     * 标记此监听
     */
    public final String tag;

    public ProgressListener(String tag, boolean download) {
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