package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;

/**
 * 监听一些核心事件
 */
public abstract class EventListener {
    EventListener NONE = new EventListener() {

    };

    static EventListener.Factory factory(final EventListener listener) {
        return new EventListener.Factory() {
            public EventListener create(Call<?> call) {
                return listener;
            }
        };
    }

    public void disposed(Call<?> call, Lifecycle.Event event) {

    }

    public void observe(LifecycleProvider provider, LifecycleProvider.Observer observer) {


    }

    public void removeObserver(LifecycleProvider provider, LifecycleProvider.Observer observer) {


    }

    public interface Factory {
        EventListener create(Call<?> call);
    }
}
