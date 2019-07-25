package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a {@link LifecycleOwner} so that it can be used as a {@link LifecycleProvider}. For example,
 * you can do
 * <pre>{@code
 * LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);
 * myObservable
 *     .compose(provider.bindLifecycle())
 *     .subscribe();
 * }</pre>
 * where {@code this} is a {@code android.arch.lifecycle.LifecycleActivity} or
 * {@code android.arch.lifecycle.LifecycleFragment}.
 */
public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    //保存最后一次的状态
    private volatile Lifecycle.Event lastEvent;

    public static LifecycleProvider createLifecycleProvider(LifecycleOwner owner) {
        return new AndroidLifecycle(owner);
    }

    private final List<WeakReference<LifecycleEvent>> lifecycleEvents = new ArrayList<>();

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        lastEvent = event;
        for (WeakReference<LifecycleEvent> weakCall : lifecycleEvents) {
            LifecycleEvent lifecycleEvent = weakCall.get();
            if (lifecycleEvent != null) {
                lifecycleEvent.onEvent(event);
            }
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void bindToLifecycle(LifecycleEvent lifecycleEvent) {
        if (lastEvent != null) {
            lifecycleEvent.onEvent(lastEvent);
        }
        WeakReference<LifecycleEvent> weakReference = new WeakReference<>(lifecycleEvent);
        lifecycleEvents.add(weakReference);

        for (WeakReference<LifecycleEvent> le : lifecycleEvents) {
            LifecycleEvent event = le.get();
            Log.e("print", "debugLog:" + event);
        }
    }
}
