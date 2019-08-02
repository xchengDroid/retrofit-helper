package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * 统一分发Activity和 Fragment的生命周期时间
 */
public interface LifecycleProvider {

    @MainThread
    void observe(@NonNull Observer<Lifecycle.Event> observer);

    @MainThread
    void removeObserver(@NonNull Observer<Lifecycle.Event> observer);

}
