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

/**
 * 实现LifecycleObserver监听Activity和Fragment的生命周期
 *
 * @see android.database.Observable
 */
public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    @GuardedBy("mObservers")
    private final ArrayList<Observer> mObservers = new ArrayList<>();
    /**
     * 缓存当前的Event事件
     */
    @GuardedBy("mObservers")
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
        //保证线程的可见性
        mEvent = event;
        synchronized (mObservers) {
            // since onChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
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
        if (mEvent != null) {
            //noinspection ConstantConditions
            observer.onChanged(mEvent);
        }
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                return;
            }
            mObservers.add(observer);
            logCount("observe");
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
                return;
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
