package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

public final class LifecycleLiveData extends LiveData<Lifecycle.Event> implements LifecycleObserver {

    public static LifecycleLiveData createLifecycleProvider(LifecycleOwner owner) {
        return new LifecycleLiveData(owner);
    }


    private LifecycleLiveData(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        setValue(event);
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<Lifecycle.Event> observer) {
        super.observeForever(observer);
    }

    @Override
    public void observeForever(@NonNull Observer<Lifecycle.Event> observer) {
        super.observeForever(observer);
    }

    @Override
    public void removeObserver(@NonNull Observer<Lifecycle.Event> observer) {
        super.removeObserver(observer);
    }

    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        super.removeObservers(owner);
    }
}
