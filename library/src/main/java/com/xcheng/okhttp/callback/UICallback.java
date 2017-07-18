package com.xcheng.okhttp.callback;

import android.support.annotation.UiThread;

import com.xcheng.okhttp.error.BaseError;

public abstract class UICallback<T> {
    @UiThread
    public void onBefore(OkCall<T> okCall) {

    }

    @UiThread
    public void onAfter(OkCall<T> okCall) {

    }

    /**
     * UI Thread
     *
     * @param progress 上传进度(0~1]
     */
    @UiThread
    public void inProgress(OkCall<T> okCall, float progress, long total, boolean done) {

    }

    /**
     * UI Thread
     *
     * @param progress 下载进度(0~1]
     */
    @UiThread
    public void outProgress(OkCall<T> okCall, float progress, long total, boolean done) {

    }

    @UiThread
    public abstract void onError(OkCall<T> okCall, BaseError error);

    @UiThread
    public abstract void onSuccess(OkCall<T> okCall, T response);

}