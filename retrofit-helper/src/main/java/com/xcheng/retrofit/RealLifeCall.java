package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

final class RealLifeCall<T> implements LifeCall<T>, LifecycleProvider.Observer {
    private final Call<T> delegate;
    private final Lifecycle.Event event;
    private final LifecycleProvider provider;
    /**
     * LifeCall是否被释放了
     */
    private final AtomicBoolean unsubscribed = new AtomicBoolean();

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
                if (!isDisposed()) {
                    callback.onStart(call);
                }
            }

            @NonNull
            @Override
            public HttpError parseThrowable(Call<T> call, Throwable t) {
                if (!isDisposed()) {
                    return callback.parseThrowable(call, t);
                }
                return new HttpError("Already disposed.", t);
            }

            @NonNull
            @Override
            public T transform(Call<T> call, T t) {
                if (!isDisposed()) {
                    return callback.transform(call, t);
                }
                return t;
            }

            @Override
            public void onSuccess(Call<T> call, T t) {
                if (!isDisposed()) {
                    callback.onSuccess(call, t);
                }
            }

            @Override
            public void onError(Call<T> call, HttpError error) {
                if (!isDisposed()) {
                    callback.onError(call, error);
                }
            }

            @Override
            public void onCompleted(Call<T> call, @Nullable Throwable t) {
                if (!isDisposed()) {
                    callback.onCompleted(call, t);
                }
                //ignore already removed
                provider.removeObserver(RealLifeCall.this);
            }
        });
    }

    @NonNull
    @Override
    public T execute() throws Throwable {
        try {
            if (isDisposed()) {
                throw new DisposedException("Already disposed.");
            }
            T body = delegate.execute();
            if (isDisposed()) {
                throw new DisposedException("Already disposed.");
            }
            return body;
        } catch (Throwable t) {
            if (isDisposed() && !(t instanceof DisposedException)) {
                throw new DisposedException("Already disposed.", t);
            }
            throw t;
        } finally {
            provider.removeObserver(this);
        }
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        if (this.event == event || event == Lifecycle.Event.ON_DESTROY) {
            dispose();
            RetrofitFactory.getOnEventListener().onDisposed(delegate, event);
        }
    }

    @Override
    public void dispose() {
        if (unsubscribed.compareAndSet(false, true)) {
            delegate.cancel();
            provider.removeObserver(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return unsubscribed.get();
    }
}