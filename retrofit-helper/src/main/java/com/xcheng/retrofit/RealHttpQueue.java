package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealHttpQueue<T> implements HttpQueue<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    RealHttpQueue(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Objects.requireNonNull(callback, "callback==null");
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.onStart(delegate);
            }
        });
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<T> call, @NonNull Response<T> response) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (delegate.isCanceled()) {  //call==delegate 局部变量访问快
                            callback.onCompleted(delegate, new IOException("Canceled"));
                            return;
                        }
                        //response.isSuccessful() 不能保证 response.body() != null,反之可以
                        T body = response.body();
                        if (body != null) {
                            callback.onSuccess(delegate, body);
                            callback.onCompleted(delegate, null);
                        } else {
                            callback.onCompleted(delegate, new HttpException(response));
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<T> call, @NonNull Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(delegate, t);
                    }
                });
            }
        });
    }

    @Override
    public Executor callbackExecutor() {
        return callbackExecutor;
    }

    @Override
    public Call<T> delegate() {
        return delegate;
    }
}