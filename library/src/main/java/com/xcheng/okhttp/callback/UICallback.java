package com.xcheng.okhttp.callback;

import com.xcheng.okhttp.error.EasyError;

public abstract class UICallback<T> {
    public void onBefore(OkCall<T> okCall) {

    }

    public void onAfter(OkCall<T> okCall) {

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