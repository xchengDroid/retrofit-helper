package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;
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
        Objects.requireNonNull(callback, "callback==null");
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.onStart(RealCall.this);
            }
        });
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<T> call, @NonNull Response<T> response) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (delegate.isCanceled()) {
                            callback.onCompleted(RealCall.this, new IOException("Canceled"));
                            return;
                        }
                        //response.isSuccessful() 不能保证 response.body() != null,反之可以
                        T body = response.body();
                        if (body != null) {
                            callback.onSuccess(RealCall.this, body);
                            callback.onCompleted(RealCall.this, null);
                        } else {
                            callback.onCompleted(RealCall.this, new HttpException(response));
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<T> call, @NonNull Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(RealCall.this, t);
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

    @SuppressWarnings("MethodDoesntCallSuperMethod") // Performing deep clone.
    @Override
    public Call<T> clone() {
        return new RealCall<>(callbackExecutor, delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }
}