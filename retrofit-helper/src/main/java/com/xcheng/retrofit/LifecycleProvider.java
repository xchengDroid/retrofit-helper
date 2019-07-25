package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;

public interface LifecycleProvider {

    /**
     * 和生命周期相关联，生命周期发生改变的时候回调用{@link LifecycleEvent#onEvent(Lifecycle.Event)}方法
     */
    void bindToLifecycle(LifecycleEvent lifecycleEvent);

}
