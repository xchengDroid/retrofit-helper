package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.ResponseParse;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.utils.Platform;

import java.io.IOException;

/**
 * Created by chengxin on 2017/6/26.
 */

final class ExecutorCallback<T> extends UICallback<T> {
    private static final Platform PLATFORM = Platform.get();
    private final UICallback<T> delegate;
    private final OkCall<T> okCall;
    private final ResponseParse<T> responseParse;
    private OnAfterListener onAfterListener;

    ExecutorCallback(UICallback<T> delegate, OkCall<T> okCall, ResponseParse<T> responseParse) {
        this.delegate = delegate;
        this.okCall = okCall;
        this.responseParse = responseParse;
    }

    @Override
    public void onBefore(final int id) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (isPostUi()) {
                    delegate.onBefore(id);
                }
            }
        });
    }

    @Override
    public void onAfter(final int id) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (isPostUi()) {
                    delegate.onAfter(id);
                }
                if (onAfterListener != null) {
                    onAfterListener.onAfter(id);
                }
            }
        });
    }

    @Override
    public void inProgress(final float progress, final long total, final int id) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (isPostUi()) {
                    delegate.inProgress(progress, total, id);
                }
            }
        });
    }

    @Override
    public void onError(@NonNull final BaseError error, final int id) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (isPostUi()) {
                    delegate.onError(error, id);
                }
            }
        });
    }

    @Override
    public void onSuccess(@NonNull final T response, final int id) {
        PLATFORM.execute(new Runnable() {
            @Override
            public void run() {
                if (!okCall.isCanceled()) {
                    delegate.onSuccess(response, id);
                } else {
                    onError(canceledError(), id);
                }
            }
        });
    }

    private boolean isPostUi() {
        return !okCall.isCanceled() || EasyOkHttp.getOkConfig().isPostUiIfCanceled();
    }

    private BaseError canceledError() {
        String errorMsg = "Call Canceled, url= " + okCall.request().url();
        BaseError error = responseParse.getError(new IOException(errorMsg));
        if (error == null) {
            error = BaseError.getNotFoundError(errorMsg);
        }
        return error;
    }

    void setOnAfterListener(OnAfterListener onAfterListener) {
        this.onAfterListener = onAfterListener;
    }

    interface OnAfterListener {
        @UiThread
        void onAfter(int id);
    }
}
