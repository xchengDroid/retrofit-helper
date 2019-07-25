package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;

public interface LifecycleProvider {

    /**
     * 和生命周期相关联，生命周期发生改变的时候回调用{@link LifecycleEvent#onEvent(Lifecycle.Event)}方法
     */
    void bindToLifecycle(@NonNull LifecycleEvent lifecycleEvent);

    /**
     * 从生命周期解绑
     */
    void unbindFromLifecycle(@NonNull LifecycleEvent lifecycleEvent);
}
