package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.utils.Platform;

/**
 * UICallBack 处理类
 * Created by chengxin on 2017/6/26.
 */

final class ExecutorCallback<T> extends UICallback<T> {
    private static final Platform PLATFORM = Platform.get();
    private final UICallback<T> delegate;
    private OnExecutorListener listener;

    ExecutorCallback(UICallback<T> delegate, OnExecutorListener listener) {
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
    public void inProgress(final OkCall<T> okCall, final float progress, final long total) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (okCall.isPostUi()) {
                    delegate.inProgress(okCall, progress, total);
                }
            }
        });
    }

    @Override
    public void onError(final OkCall<T> okCall, final BaseError error) {
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
                    onError(okCall, listener.canceledError());
                }
            }
        });
    }

    interface OnExecutorListener {
        void onAfter();

        /**
         * 如果请求被取消，获取所需的Canceled Error
         *
         * @return canceledError
         */
        @NonNull
        BaseError canceledError();
    }
}
