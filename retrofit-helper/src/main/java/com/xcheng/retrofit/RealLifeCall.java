package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

final class RealLifeCall<T> implements LifeCall<T> {
    private final Call<T> delegate;
    private final Lifecycle.Event event;
    private final LifecycleProvider provider;
    /**
     * LifeCall是否被释放了
     */
    private volatile boolean disposed;

    RealLifeCall(Call<T> delegate, Lifecycle.Event event, LifecycleProvider provider) {
        this.delegate = delegate;
        this.event = event;
        this.provider = provider;
        provider.observe(this);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        delegate.enqueue(new Callback<T>() {
            @Override
            public void onStart(Call<T> call) {
                if (!disposed) {
                    callback.onStart(call);
                }
            }

            @NonNull
            @Override
            public HttpError parseThrowable(Call<T> call, Throwable t) {
                if (!disposed) {
                    return callback.parseThrowable(call, t);
                }
                //for not crash
                return new HttpError("Already disposed.", t);
            }

            @NonNull
            @Override
            public T transform(Call<T> call, T t) {
                if (!disposed) {
                    return callback.transform(call, t);
                }
                //for not crash
                return t;
            }

            @Override
            public void onSuccess(Call<T> call, T t) {
                if (!disposed) {
                    callback.onSuccess(call, t);
                }
            }

            @Override
            public void onError(Call<T> call, HttpError error) {
                if (!disposed) {
                    callback.onError(call, error);
                }
            }

            @Override
            public void onCompleted(Call<T> call, @Nullable Throwable t) {
                if (!disposed) {
                    callback.onCompleted(call, t);
                    //防止重复调用
                    provider.removeObserver(RealLifeCall.this);
                }
            }
        });
    }

    @NonNull
    @Override
    public T execute() throws Throwable {
        try {
            if (disposed) {
                throw new DisposedException("Already disposed.");
            }
            T body = delegate.execute();
            if (disposed) {
                throw new DisposedException("Already disposed.");
            }
            return body;
        } catch (Throwable t) {
            if (disposed && !(t instanceof DisposedException)) {
                throw new DisposedException("Already disposed.", t);
            }
            throw t;
        } finally {
            if (!disposed) {
                //防止重复调用
                provider.removeObserver(this);
            }
        }
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        //just in case
        if (disposed)
            return;
        if (this.event == event || event == Lifecycle.Event.ON_DESTROY) {
            disposed = true;
            delegate.cancel();
            if (RetrofitFactory.LISTENER != null) {
                RetrofitFactory.LISTENER.onDisposed(delegate, event);
            }
            provider.removeObserver(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}