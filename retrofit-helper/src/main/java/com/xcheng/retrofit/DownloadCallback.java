package com.xcheng.retrofit;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * 监听下载进度 {@link Callback}
 *
 * @param <T> Successful response body type.
 */
public interface DownloadCallback<T> {
    @Nullable
    @WorkerThread
    T convert(DownloadCall<T> call, ResponseBody value) throws IOException;

    /**
     * 防止频繁调用{@link #onDownload},每次下载增加多少(0-1),否则默认使用0.01
     *
     * @return increase percent
     */
    @WorkerThread
    float eachDownloadIncrease();

    void onDownload(DownloadCall<T> call, long progress, long contentLength, boolean done);

    void onError(DownloadCall<T> call, Throwable t);

    void onSuccess(DownloadCall<T> call, T t);
}