package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.Platform;

/**
 * UICallBack 处理类
 * Created by chengxin on 2017/6/26.
 */
final class PostCallback<T> extends UICallback<T> {
    private static final Platform PLATFORM = Platform.get();
    private final UICallback<T> delegate;
    private final OnFinishedListener listener;

    PostCallback(UICallback<T> delegate, OnFinishedListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public void onStart(final OkCall<T> okCall) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.onStart(okCall);
                }
            }
        });
    }

    @Override
    public void onFinish(final OkCall<T> okCall) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.onFinish(okCall);
                }
                listener.onFinished();
            }
        });
    }

    @Override
    public void inProgress(final OkCall<T> okCall, final float progress, final long total, final boolean done) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.inProgress(okCall, progress, total, done);
                }
            }
        });
    }

    @Override
    public void outProgress(final OkCall<T> okCall, final float progress, final long total, final boolean done) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.outProgress(okCall, progress, total, done);
                }
            }
        });
    }

    @Override
    public void onError(final OkCall<T> okCall, final EasyError error) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.onError(okCall, error);
                }
            }
        });
    }

    @Override
    public void onSuccess(final OkCall<T> okCall, final T response) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    if (!okCall.isCanceled()) {
                        //如果请求未被取消，回调主线程onSuccess函数
                        delegate.onSuccess(okCall, response);
                    } else {
                        //如果请求被取消，回调onError函数
                        delegate.onError(okCall, new EasyError("Canceled"));
                    }
                }
            }
        });
    }

    /**
     * 请求已经结束回调
     */
    interface OnFinishedListener {
        void onFinished();
    }
}
