package com.xcheng.retrofit;

import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealDownloadCall<T> implements DownloadCall<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<ResponseBody> delegate;

    RealDownloadCall(Executor callbackExecutor, retrofit2.Call<ResponseBody> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        delegate.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.body() == null) {
                    callFailure(new HttpException(response));
                    return;
                }
                ResponseBody responseBody = new ProgressResponseBody(response.body()) {
                    @Override
                    protected void onDownload(long progress, long contentLength, boolean done) {
                        callProgress(progress, contentLength, done);
                    }
                };
                try {
                    @Nullable
                    T body = callback.convert(RealDownloadCall.this, responseBody);
                    if (body != null) {
                        callSuccess(body);
                        return;
                    }
                    callFailure(new NullPointerException("callback.convert return null"));
                } catch (Throwable t) {
                    callFailure(t);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, final Throwable t) {
                callFailure(t);
            }

            private void callProgress(final long progress, final long contentLength, final boolean done) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownload(RealDownloadCall.this, progress, contentLength, done);
                    }
                });
            }

            private void callSuccess(final T body) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(RealDownloadCall.this, body);
                    }
                });
            }

            private void callFailure(final Throwable e) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(RealDownloadCall.this, e);
                    }
                });
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
    @Override
    public DownloadCall<T> clone() {
        return new RealDownloadCall<>(callbackExecutor, delegate.clone());
    }
}