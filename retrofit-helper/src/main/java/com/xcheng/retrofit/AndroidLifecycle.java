package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.GuardedBy;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    @GuardedBy("mObservers")
    private final ArrayList<Observer> mObservers = new ArrayList<>();
    /**
     * 缓存当前的Event事件
     */
    @Nullable
    private volatile Lifecycle.Event mEvent;

    @MainThread
    public static LifecycleProvider createLifecycleProvider(LifecycleOwner owner) {
        return new AndroidLifecycle(owner);
    }

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        mEvent = event;
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged(event);
            }
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void observe(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                throw new IllegalStateException("Observer " + observer + " is already registered.");
            }
            mObservers.add(observer);
            Lifecycle.Event event = mEvent;
            if (event != null) {
                observer.onChanged(event);
            }
            logCount("observer");
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized (mObservers) {
            int index = mObservers.indexOf(observer);
            if (index == -1) {
                throw new IllegalStateException("Observer " + observer + " was not registered.");
            }
            mObservers.remove(index);
            logCount("removeObserver");
        }
    }

    private void logCount(String prefix) {
        if (RetrofitFactory.SHOW_LOG) {
            Log.d(LifeCall.TAG, prefix + " -->" + mObservers.size());
        }
    }
}
