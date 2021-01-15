package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Response;

final class RealHttpQueue<T> implements HttpQueue<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    RealHttpQueue(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public void enqueue(@Nullable LifecycleOwner owner, Callback<T> callback) {
        Objects.requireNonNull(callback, "callback==null");
        if (owner != null) {
            if (OptionalExecutor.isMainThread()) {
                throw new IllegalStateException("Cannot invoke enqueue with LifecycleOwner on a background"
                        + " thread");
            }
            if (callbackExecutor != OptionalExecutor.getMainThreadExecutor()) {
                throw new IllegalStateException("callbackExecutor must be a MainThreadExecutor");
            }
            callback = new LifecycleCallback<>(this, callback, owner);
        }
        enqueue(callback);
    }

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
}