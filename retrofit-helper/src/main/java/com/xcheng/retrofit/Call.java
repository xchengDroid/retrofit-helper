package com.xcheng.retrofit;

import androidx.lifecycle.Lifecycle;

import okhttp3.Request;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 *
 * @param <T> Successful response body type.
 */
public interface Call<T> extends Callable<T>, Cloneable {

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

    Request request();

    /**
     * 绑定生命周期
     *
     * @param provider LifecycleProvider
     * @param event    {@link Lifecycle.Event}, {@link Lifecycle.Event#ON_ANY} is not allowed
     * @return LifeCall
     */
    LifeCall<T> bindToLifecycle(LifecycleProvider provider, Lifecycle.Event event);

    /**
     * default event is {@link Lifecycle.Event#ON_DESTROY}
     *
     * @param provider LifecycleProvider
     * @return LifeCall
     * @see #bindToLifecycle(LifecycleProvider, Lifecycle.Event)
     */
    LifeCall<T> bindUntilDestroy(LifecycleProvider provider);
}
