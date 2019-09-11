package com.xcheng.retrofit;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持下载功能{@link retrofit2.Call}
 * <p>
 * Note: 如果文件过大，防止retrofit将文件读入内存导致内存溢出，请使用{@link retrofit2.http.Streaming}注解
 *
 * @param <T> Successful response body type.
 */
public interface DownloadCall<T> extends Cloneable {
    void enqueue(Callback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    DownloadCall<T> clone();

    Request request();

    interface Callback<T> {
        @Nullable
        @WorkerThread
        T convert(DownloadCall<T> call, ResponseBody value) throws IOException;

        void onDownload(DownloadCall<T> call, long progress, long contentLength, boolean done);

        void onError(DownloadCall<T> call, Throwable t);

        void onSuccess(DownloadCall<T> call, T t);
    }
}
