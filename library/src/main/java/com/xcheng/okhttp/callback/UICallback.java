package com.xcheng.okhttp.callback;

import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.OkCall;

/**
 * {@link com.xcheng.okhttp.request.ExecutorCall} http请求回调函数
 *
 * @param <T>
 */
public abstract class UICallback<T> {
    public void onStart(OkCall<T> okCall) {

    }

    public void onFinish(OkCall<T> okCall) {

    }

    /**
     * @param progress 上传进度(0~1]
     */
    public void inProgress(OkCall<T> okCall, float progress, long total, boolean done) {

    }

    /**
     * @param progress 下载进度(0~1]
     */
    public void outProgress(OkCall<T> okCall, float progress, long total, boolean done) {

    }

    public abstract void onError(OkCall<T> okCall, EasyError error);

    public abstract void onSuccess(OkCall<T> okCall, T response);

}