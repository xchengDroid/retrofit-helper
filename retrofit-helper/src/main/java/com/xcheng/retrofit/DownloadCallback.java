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

    void onDownload(DownloadCall<T> call, long progress, long contentLength, boolean done);

    void onError(DownloadCall<T> call, Throwable t);

    void onSuccess(DownloadCall<T> call, T t);
}