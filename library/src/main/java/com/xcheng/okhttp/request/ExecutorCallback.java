package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.Platform;

/**
 * UICallBack 处理类
 * Created by chengxin on 2017/6/26.
 */
final class ExecutorCallback<T> extends UICallback<T> {
    private static final Platform PLATFORM = Platform.get();
    private final UICallback<T> delegate;
    private OnAfterListener listener;

    ExecutorCallback(UICallback<T> delegate, OnAfterListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public void onBefore(final OkCall<T> okCall) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.onBefore(okCall);
                }
            }
        });
    }

    @Override
    public void onAfter(final OkCall<T> okCall) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.onAfter(okCall);
                }
                listener.onAfter();
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
                if (!okCall.isCanceled()) {
                    delegate.onSuccess(okCall, response);
                } else {
                    onError(okCall, EasyError.create("Canceled"));
                }
            }
        });
    }

    interface OnAfterListener {
        void onAfter();
    }
}
