package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    private final MutableLiveData<Lifecycle.Event> mLiveData = new MutableLiveData<>();

    public static LifecycleProvider createLifecycleProvider(LifecycleOwner owner) {
        return new AndroidLifecycle(owner);
    }

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        mLiveData.setValue(event);
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void observe(@NonNull Observer<Lifecycle.Event> observer) {
        mLiveData.observeForever(observer);
    }

    @Override
    public void removeObserver(@NonNull Observer<Lifecycle.Event> observer) {
        mLiveData.removeObserver(observer);
    }
}