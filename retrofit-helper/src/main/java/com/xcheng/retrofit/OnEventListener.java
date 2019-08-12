package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;

/**
 * 监听一些核心事件
 */
public abstract class OnEventListener {
    OnEventListener NONE = new OnEventListener() {

    };

    static OnEventListener.Factory factory(final OnEventListener listener) {
        return new OnEventListener.Factory() {
            public OnEventListener create(Call<?> call) {
                return listener;
            }
        };
    }

    public void onDisposed(Call<?> call, Lifecycle.Event event) {

    }

    public void onObserve(LifecycleProvider provider, LifecycleProvider.Observer observer) {


    }

    public void onRemoveObserver(LifecycleProvider provider, LifecycleProvider.Observer observer) {


    }

    public interface Factory {
        OnEventListener create(Call<?> call);
    }
}
