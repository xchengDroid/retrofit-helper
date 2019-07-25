package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.Nullable;

public interface LifecycleEvent {
    void onEvent(@Nullable Lifecycle.Event event);
}
