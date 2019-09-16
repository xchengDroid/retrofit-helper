package com.xcheng.retrofit;

import okhttp3.Request;

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

    void enqueue(DownloadCallback<T> callback);

    boolean isExecuted();

    /**
     * 是否调用显示进度{@link DownloadCallback#onProgress(DownloadCall, long, long, boolean)}
     * 默认为true,可以用于暂停显示进度，比如当Activity调用了onPause()方法等
     */
    void callProgress(boolean callProgress);

    void cancel();

    boolean isCanceled();

    DownloadCall<T> clone();

    Request request();
}
