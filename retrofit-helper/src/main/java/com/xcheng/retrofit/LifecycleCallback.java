package com.xcheng.retrofit;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建时间：2020-07-29
 * 编写人： chengxin
 * 功能描述：生命周期回调
 */
final class LifecycleCallback<T> implements Callback<T>, LifecycleObserver {
    private final Call<T> call;
    private final Callback<T> delegate;
    private final LifecycleOwner owner;
    /**
     * LifeCall是否被释放了
     * like rxAndroid MainThreadDisposable or rxJava ObservableUnsubscribeOn, IoScheduler
     */
    private final AtomicBoolean once = new AtomicBoolean();

    @MainThread
    LifecycleCallback(Call<T> call, Callback<T> delegate, LifecycleOwner owner) {
        this.call = call;
        this.delegate = delegate;
        this.owner = owner;
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            //发起请求的时候Owner是否已经销毁了
            //此时注册生命周期监听不会回调了onDestroy Event
            once.set(true);
            call.cancel();
        } else {
            owner.getLifecycle().addObserver(this);
        }
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
    public void onCompleted(Call<T> call, @Nullable Throwable t) {
        if (!once.get()) {
            delegate.onCompleted(call, t);
            owner.getLifecycle().removeObserver(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onChanged(LifecycleOwner owner, @NonNull Lifecycle.Event event) {
        // 事件ordinal小于等于当前调用？
        if (event == Lifecycle.Event.ON_DESTROY && once.compareAndSet(false, true)) {
            call.cancel();
            owner.getLifecycle().removeObserver(this);
        }
    }
}
