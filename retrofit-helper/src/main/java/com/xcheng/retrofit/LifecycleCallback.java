package com.xcheng.retrofit;

import androidx.annotation.NonNull;
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
    private final HttpQueue<T> httpQueue;
    private final Callback<T> delegate;
    private final LifecycleOwner owner;
    /**
     * LifeCall是否被释放了
     * like rxAndroid MainThreadDisposable or rxJava ObservableUnsubscribeOn, IoScheduler
     */
    private final AtomicBoolean once = new AtomicBoolean();

    LifecycleCallback(HttpQueue<T> httpQueue, Callback<T> delegate, LifecycleOwner owner) {
        this.httpQueue = httpQueue;
        this.delegate = delegate;
        this.owner = owner;
        OptionalExecutor.get().executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                    //发起请求的时候Owner是否已经销毁了
                    //此时注册生命周期监听不会回调了onDestroy Event
                    once.set(true);
                    httpQueue.delegate().cancel();
                } else {
                    owner.getLifecycle().addObserver(LifecycleCallback.this);
                }
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
            OptionalExecutor.get().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    owner.getLifecycle().removeObserver(LifecycleCallback.this);
                }
            });
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onChanged(LifecycleOwner owner, @NonNull Lifecycle.Event event) {
        //事件ordinal小于等于当前调用？
        //liveData 也会在onDestroy时释放所有的Observer
        if (event == Lifecycle.Event.ON_DESTROY && once.compareAndSet(false, true)) {
            httpQueue.delegate().cancel();
            owner.getLifecycle().removeObserver(this);
        }
    }
}
