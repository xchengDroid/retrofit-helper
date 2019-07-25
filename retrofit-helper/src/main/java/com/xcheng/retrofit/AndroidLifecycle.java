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

    private final List<WeakReference<LifeCall<?>>> weakLifeCalls = new ArrayList<>();

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        lastEvent = event;
        for (WeakReference<LifeCall<?>> weakCall : weakLifeCalls) {
            LifeCall<?> lifeCall = weakCall.get();
            if (lifeCall != null) {
                lifeCall.onEvent(event);
            }
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void bindLifecycle(LifeCall<?> call) {
        if (lastEvent != null) {
            call.onEvent(lastEvent);
        }
        WeakReference<LifeCall<?>> lifeCall = new WeakReference<LifeCall<?>>(call);
        weakLifeCalls.add(lifeCall);

        for (WeakReference<LifeCall<?>> weakCall : weakLifeCalls) {
            LifeCall<?> call1 = weakCall.get();
            Log.e("print", "debugLog:" + call1);
        }
    }
}
