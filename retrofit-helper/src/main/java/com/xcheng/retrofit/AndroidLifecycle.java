package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class AndroidLifecycle implements LifecycleProvider, LifecycleObserver {

    LiveData<Lifecycle.Event> liveData = new MutableLiveData<>();
    //保存最后一次的状态
    private Lifecycle.Event savedLastEvent;

    public static LifecycleProvider createLifecycleProvider(LifecycleOwner owner) {
        return new AndroidLifecycle(owner);
    }

    private final List<WeakReference<LifecycleEvent>> lifecycleEvents = new ArrayList<>();

    private AndroidLifecycle(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    void onEvent(LifecycleOwner owner, Lifecycle.Event event) {
        savedLastEvent = event;
        for (WeakReference<LifecycleEvent> weakReference : lifecycleEvents) {
            LifecycleEvent lifecycleEvent = weakReference.get();
            if (lifecycleEvent != null) {
                lifecycleEvent.onEvent(event);
            }
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            owner.getLifecycle().removeObserver(this);
        }
    }

    @Override
    public void bindToLifecycle(@NonNull LifecycleEvent lifecycleEvent) {

        liveData.observeForever(new Observer<Lifecycle.Event>() {
            @Override
            public void onChanged(@Nullable Lifecycle.Event event) {

            }
        });


        for (int index = 0; index < lifecycleEvents.size(); index++) {
            WeakReference<LifecycleEvent> weakReference = lifecycleEvents.get(index);
            LifecycleEvent existing = weakReference.get();
            Log.e("print", "existing=" + existing);
            if (existing == null) {
                lifecycleEvents.remove(weakReference);
                index--;
            } else if (existing == lifecycleEvent) {
                return;
            }
            Log.e("print", "size=" + lifecycleEvents.size());
        }
        if (savedLastEvent != null) {
            lifecycleEvent.onEvent(savedLastEvent);
        }
        lifecycleEvents.add(new WeakReference<>(lifecycleEvent));
    }
}
