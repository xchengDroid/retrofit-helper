package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    //保存最后一次的状态
    private Lifecycle.Event lastEvent;

    public static LifecycleProvider createLifecycleProvider(LifecycleOwner owner) {
        return new AndroidLifecycle(owner);
    }

    private final List<LifecycleEvent> lifecycleEvents = new ArrayList<>();

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        lastEvent = event;
        for (LifecycleEvent lifecycleEvent : lifecycleEvents) {
            lifecycleEvent.onEvent(event);
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void unbindFromLifecycle(@NonNull LifecycleEvent lifecycleEvent) {
        lifecycleEvents.remove(lifecycleEvent);
    }

    @Override
    public void bindToLifecycle(@NonNull LifecycleEvent lifecycleEvent) {
        if (lifecycleEvents.contains(lifecycleEvent))
            return;
        if (lastEvent != null) {
            lifecycleEvent.onEvent(lastEvent);
        }
        lifecycleEvents.add(lifecycleEvent);
    }
}
