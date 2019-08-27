package com.simple.okhttp;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xcheng.retrofit.LifecycleProvider;
import com.xcheng.retrofit.NetTaskExecutor;

/**
 * 创建时间：2019-08-26
 * 编写人： chengxin
 * 功能描述：线程调度类
 */
public final class ToMainThread implements LifecycleProvider.Observer {
    @Nullable
    private volatile Lifecycle.Event mEvent;
    private final LifecycleProvider provider;

    public ToMainThread(LifecycleProvider provider) {
        this.provider = provider;
        provider.observe(this);
    }

    public void to(@NonNull final Runnable runnable, final Lifecycle.Event event) {
        NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (mEvent == event || mEvent == Lifecycle.Event.ON_DESTROY)
                    return;
                runnable.run();
            }
        });

    }

    public void toDelayed(@NonNull final Runnable runnable, final Lifecycle.Event event, long delayMillis) {
        NetTaskExecutor.getInstance().postToMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (mEvent == event || mEvent == Lifecycle.Event.ON_DESTROY)
                    return;
                runnable.run();
            }
        }, delayMillis);
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        this.mEvent = event;
        if (event == Lifecycle.Event.ON_DESTROY) {
            provider.removeObserver(this);
        }
    }
}
