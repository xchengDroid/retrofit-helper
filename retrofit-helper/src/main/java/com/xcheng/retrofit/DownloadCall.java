package com.xcheng.retrofit;

import androidx.annotation.FloatRange;

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

    /**
     * The default implementation delegates to {@link #enqueue(float, DownloadCallback)}.
     **/
    void enqueue(DownloadCallback<T> callback);

    /**
     * @param increaseOfProgress 每次调用{@link DownloadCallback#onProgress(DownloadCall, long, long, boolean)}
     *                           progress必须增加 {@code contentLength*increaseOfProgress},范围为（0-1).
     */
    void enqueue(@FloatRange(from = 0, to = 1, fromInclusive = false, toInclusive = false) float increaseOfProgress,
                 DownloadCallback<T> callback);


    boolean isExecuted();

    /**
     * 暂停显示进度{@link DownloadCallback#onProgress(DownloadCall, long, long, boolean)}
     * 默认为true,可以用于暂停显示进度，比如当Activity调用了onPause()方法等
     */
    void pauseProgress();

    void resumeProgress();

    void cancel();

    boolean isCanceled();

    DownloadCall<T> clone();

    Request request();
}
