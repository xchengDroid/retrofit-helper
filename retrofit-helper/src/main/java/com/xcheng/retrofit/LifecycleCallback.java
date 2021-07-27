package com.xcheng.retrofit;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 创建时间：2020-07-29
 * 编写人： chengxin
 * 功能描述：生命周期回调
 */
final class LifecycleCallback<T> implements Callback<T>, LifecycleObserver {
    private final CompletableCall<T> completableCall;
    private final Callback<T> delegate;
    private final LifecycleOwner owner;
    private final Lifecycle.Event event;
    /**
     * LifeCall是否被释放了
     * like rxAndroid MainThreadDisposable or rxJava ObservableUnsubscribeOn, IoScheduler
     */
    private final AtomicBoolean once = new AtomicBoolean();

    LifecycleCallback(CompletableCall<T> completableCall, Callback<T> delegate, LifecycleOwner owner, @Nullable Lifecycle.Event event) {
        this.completableCall = completableCall;
        this.delegate = delegate;
        this.event = event == null ? Lifecycle.Event.ON_DESTROY : event;
        this.owner = owner;
        OptionalExecutor.get().executeOnMainThread(() -> {
            if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                //发起请求的时候Owner是否已经销毁了
                //此时注册生命周期监听不会回调了onDestroy Event
                once.set(true);
                completableCall.delegate().cancel();
            } else {
                owner.getLifecycle().addObserver(LifecycleCallback.this);
            }
        });
    }

    @Override
    public void onStart(Call<T> call) {
        if (!once.get()) {
            delegate.onStart(call);
        }
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!once.get()) {
            delegate.onResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (!once.get()) {
            delegate.onFailure(call, t);
        }
    }

    @Override
    public void onCompleted(Call<T> call) {
        if (!once.get()) {
            delegate.onCompleted(call);
            OptionalExecutor.get().executeOnMainThread(() ->
                    owner.getLifecycle().removeObserver(LifecycleCallback.this));
        }
    }

    @MainThread
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onChanged(LifecycleOwner owner, @NonNull Lifecycle.Event event) {
        //事件ordinal小于等于当前调用？
        //liveData 也会在onDestroy时释放所有的Observer
        if (event.compareTo(this.event) >= 0 && once.compareAndSet(false, true)) {
            completableCall.delegate().cancel();
            owner.getLifecycle().removeObserver(this);
        }
    }
}
