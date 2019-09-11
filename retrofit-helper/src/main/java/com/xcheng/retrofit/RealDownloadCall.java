package com.xcheng.retrofit;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.concurrent.Executor;

import okhttp3.Request;
import okhttp3.ResponseBody;
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
                try {
                    ResponseBody responseBody = new ProgressResponseBody(response.body()) {
                        @Override
                        protected void onDownload(long progress, long contentLength, boolean done) {
                            callProgress(progress, contentLength, done);
                        }
                    };
                    @Nullable
                    T body = callback.convert(RealDownloadCall.this, responseBody);
                    if (body != null) {
                        callResult(body, null);
                        return;
                    }
                    callResult(null, new NullPointerException("callback.convert return null"));
                } catch (Throwable t) {
                    callResult(null, t);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, final Throwable t) {
                callResult(null, t);
            }

            private void callProgress(final long progress, final long contentLength, final boolean done) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownload(RealDownloadCall.this, progress, contentLength, done);
                    }
                });
            }

            @UiThread
            private void callResult(@Nullable final T body, @Nullable final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (body != null) {
                            callback.onSuccess(RealDownloadCall.this, body);
                        } else {
                            callback.onError(RealDownloadCall.this, t);
                        }
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