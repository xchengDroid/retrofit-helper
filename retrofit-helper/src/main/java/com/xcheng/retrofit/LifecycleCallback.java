package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建时间：2020-07-29
 * 编写人： chengxin
 * 功能描述：生命周期回调
 */
final class LifecycleCallback<T> implements Callback<T>, LifecycleProvider.Observer {
    private final Call<T> call;
    private final Callback<T> delegate;
    private final LifecycleProvider provider;
    private final Lifecycle.Event event;
    /**
     * LifeCall是否被释放了
     * like rxAndroid MainThreadDisposable or rxJava ObservableUnsubscribeOn, IoScheduler
     */
    private final AtomicBoolean once = new AtomicBoolean();

    LifecycleCallback(Call<T> call, @NonNull Callback<T> delegate, @NonNull LifecycleProvider provider, @NonNull Lifecycle.Event event) {
        this.call = call;
        this.delegate = delegate;
        this.provider = provider;
        this.event = event;
        this.provider.observe(this);
    }

    @Override
    public void onStart(Call<T> call) {
        if (!once.get()) {
            delegate.onStart(call);
        }
    }

    @NonNull
    @Override
    public HttpError parseThrowable(Call<T> call, Throwable t) {
        if (!once.get()) {
            return delegate.parseThrowable(call, t);
        }
        return new HttpError("Already disposed.", t);
    }

    @Nullable
    @Override
    public T onFilter(Call<T> call, T t) {
        if (!once.get()) {
            return delegate.onFilter(call, t);
        }
        return t;
    }

    @Override
    public void onError(Call<T> call, HttpError error) {
        if (!once.get()) {
            delegate.onError(call, error);
        }
    }

    @Override
    public void onSuccess(Call<T> call, T t) {
        if (!once.get()) {
            delegate.onSuccess(call, t);
        }
    }

    @Override
    public void onDispose(Lifecycle.Event event) {
        call.cancel();
        delegate.onDispose(event);
        this.provider.removeObserver(this);
    }

    @Override
    public void onCompleted(Call<T> call, @Nullable Throwable t) {
        if (!once.get()) {
            delegate.onCompleted(call, t);
            this.provider.removeObserver(this);
        }
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        if (this.event == event && once.compareAndSet(false, true)) {
            onDispose(event);
        }
    }
}
