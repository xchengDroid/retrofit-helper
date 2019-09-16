package com.xcheng.retrofit;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealDownloadCall<T> implements DownloadCall<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<ResponseBody> delegate;
    private volatile boolean pauseProgress;//default false

    RealDownloadCall(Executor callbackExecutor, retrofit2.Call<ResponseBody> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public void enqueue(final DownloadCallback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        delegate.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() == null) {
                        callFailure(new HttpException(response));
                        return;
                    }
                    ResponseBody responseBody = new ProgressResponseBody(response.body()) {
                        long lastProgress;

                        @Override
                        protected void onDownload(long progress, long contentLength, boolean done) {
                            if (pauseProgress)
                                return;
                            if (progress - lastProgress > 0.01f * contentLength || done) {
                                lastProgress = progress;
                                callProgress(progress, contentLength, done);
                            }
                        }
                    };

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
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                callFailure(t);
            }

            private void callProgress(final long progress, final long contentLength, final boolean done) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onProgress(RealDownloadCall.this, progress, contentLength, done);
                    }
                });
            }

            private void callSuccess(@NonNull final T body) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(RealDownloadCall.this, body);
                    }
                });
            }

            private void callFailure(@NonNull final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(RealDownloadCall.this, t);
                    }
                });
            }
        });
    }

    @Override
    public void pauseProgress() {
        pauseProgress = true;
    }

    @Override
    public void resumeProgress() {
        pauseProgress = false;
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