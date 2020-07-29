package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

/**
 * 创建时间：2020-07-29
 * 编写人： chengxin
 * 功能描述：生命周期回调
 */
class LifecycleCallback<T> implements Callback<T>, LifecycleProvider.Observer {

    private final Callback<T> delegate;
    private final LifecycleProvider provider;
    private final Lifecycle.Event event;

    public LifecycleCallback(Callback<T> delegate, LifecycleProvider provider, Lifecycle.Event event) {
        this.delegate = delegate;
        this.provider = provider;
        this.event = event;
    }

    @Override
    public void onStart(Call<T> call) {
        delegate.onStart(call);
    }

    @NonNull
    @Override
    public HttpError parseThrowable(Call<T> call, Throwable t) {
        return delegate.parseThrowable(call, t);
    }

    @NonNull
    @Override
    public T transform(Call<T> call, T t) {
        return delegate.transform(call, t);
    }

    @Override
    public void onError(Call<T> call, HttpError error) {
        delegate.onError(call, error);
    }

    @Override
    public void onSuccess(Call<T> call, T t) {
        delegate.onSuccess(call, t);
    }

    @Override
    public void onCompleted(Call<T> call, @Nullable Throwable t) {
        delegate.onCompleted(call, t);
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        
    }
}
