package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.OkCall;
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

    ExecutorCallback(UICallback<T> delegate, OkCall<T> okCall) {
        this.delegate = delegate;
        this.okCall = okCall;
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
                    BaseError error = okCall.getResponseParse().getError(new IOException("Call Canceled, url=" + okCall.request().url()));
                    onError(error, id);
                }
            }
        });
    }

    private boolean isPostUi() {
        return !okCall.isCanceled() || EasyOkHttp.getOkConfig().isPostUiIfCanceled();
    }
}
