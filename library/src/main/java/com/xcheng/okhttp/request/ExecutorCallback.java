package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.utils.Platform;

/**
 * Created by chengxin on 2017/6/26.
 */

final class ExecutorCallback<T> extends UICallback<T> {
    private static final Platform PLATFORM = Platform.get();
    private final UICallback<T> delegate;
    private OnAfterListener onAfterListener;

    ExecutorCallback(UICallback<T> delegate) {
        this.delegate = delegate;
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
                if (onAfterListener != null) {
                    onAfterListener.onAfter();
                }
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
                    onError(okCall, BaseError.createDefaultError("Call Canceled, url= " + okCall.request().url()));
                }
            }
        });
    }

    void setOnAfterListener(OnAfterListener onAfterListener) {
        this.onAfterListener = onAfterListener;
    }

    interface OnAfterListener {
        void onAfter();
    }
}
