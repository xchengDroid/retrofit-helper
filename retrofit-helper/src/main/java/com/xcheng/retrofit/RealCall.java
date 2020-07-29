package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealCall<T> implements Call<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    RealCall(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @NonNull
    @Override
    public T execute() throws Throwable {
        Response<T> response = delegate.execute();
        T body = response.body();
        if (body != null) {
            return body;
        }
        throw new HttpException(response);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.onStart(RealCall.this);
            }
        });
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(retrofit2.Call<T> call, Response<T> response) {
                //response.isSuccessful() 不能保证 response.body() != null,反之可以
                T body = response.body();
                if (body != null) {
                    //过滤数据
                    body = callback.onFilter(RealCall.this, body);
                }
                if (body != null) {
                    callSuccess(body);
                } else {
                    callFailure(new HttpException(response));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<T> call, Throwable t) {
                callFailure(t);
            }

            private void callSuccess(@NonNull final T body) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(RealCall.this, body);
                        callback.onCompleted(RealCall.this, null);
                    }
                });
            }

            private void callFailure(@NonNull final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        HttpError error = callback.parseThrowable(RealCall.this, t);
                        //noinspection ConstantConditions
                        Utils.checkNotNull(error == null, "error==null");
                        callback.onError(RealCall.this, error);
                        callback.onCompleted(RealCall.this, t);
                    }
                });
            }
        });
    }

    @Override
    public void enqueue(LifecycleProvider provider, Lifecycle.Event event, Callback<T> callback) {
        Utils.checkNotNull(provider, "provider==null");
        Utils.checkNotNull(event, "event==null");
        enqueue(new LifecycleCallback<>(callback, provider, event));
    }

    @Override
    public void enqueue(LifecycleProvider provider, Callback<T> callback) {
        enqueue(provider, Lifecycle.Event.ON_DESTROY, callback);
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

    @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
    @Override
    public Call<T> clone() {
        return new RealCall<>(callbackExecutor, delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }
}