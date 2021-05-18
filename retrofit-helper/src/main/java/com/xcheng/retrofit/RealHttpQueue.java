package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Response;

public final class RealHttpQueue<T> implements HttpQueue<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    RealHttpQueue(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    //@Override
    //public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
    //    boolean signalledCallback = false;
    //    try {
    //        if (response.body() == null) {
    //            signalledCallback = true;
    //            callFailure(new HttpException(response));
    //            return;
    //        }
    //        ResponseBody responseBody = new ProgressResponseBody(response.body()) {
    //            long lastProgress;
    //
    //            @Override
    //            protected void onDownload(long progress, long contentLength, boolean done) {
    //                if (pauseProgress)
    //                    return;
    //                if (progress - lastProgress > increaseOfProgress * contentLength || done) {
    //                    lastProgress = progress;
    //                    callProgress(progress, contentLength, done);
    //                }
    //            }
    //        };
    //        T body = callback.convert(RealDownloadCall.this, responseBody);
    //        signalledCallback = true;
    //        if (body != null) {
    //            callSuccess(body);
    //        } else {
    //            callFailure(new NullPointerException("callback.convert return null"));
    //        }
    //    } catch (Throwable t) {
    //        //如果为OptionalExecutor，不存在线程调度的情况下callXXX方法可能会在当前线程抛出异常
    //        //会导致callFailure方法调用两次
    //        //参考okhttp3.RealCall
    //        //参考 FutureTask#run方法ran标记
    //        if (!signalledCallback) {
    //            callFailure(t);
    //        } else {
    //            Log.w(RetrofitFactory.TAG, "Callback failure", t);
    //        }
    //    }
    //}

    @Override
    public void enqueue(final Callback<T> callback) {
        Objects.requireNonNull(callback, "callback==null");
        callbackExecutor.execute(() -> callback.onStart(delegate));
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<T> call, @NonNull Response<T> response) {
                //大道至简
                callbackExecutor.execute(() -> {
                    if (delegate.isCanceled()) {
                        // Emulate OkHttp's behavior of throwing/delivering an IOException on
                        // cancellation.
                        callback.onFailure(delegate, new IOException("Canceled"));
                    } else {
                        callback.onResponse(delegate, response);
                    }
                    callback.onCompleted(delegate);
                });
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<T> call, @NonNull Throwable t) {
                callbackExecutor.execute(() -> {
                    callback.onFailure(delegate, t);
                    callback.onCompleted(delegate);
                });
            }
        });
    }

    @Override
    public Call<T> delegate() {
        return delegate;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public HttpQueue<T> clone() {
        return new RealHttpQueue<>(callbackExecutor, delegate.clone());
    }
}