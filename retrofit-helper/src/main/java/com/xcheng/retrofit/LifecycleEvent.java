package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;

public interface LifecycleEvent {

    /**
     * 当生命周期发生改变时回调
     *
     * @param event 当前的生命周期
     */
    void onEvent(@NonNull Lifecycle.Event event);
}
