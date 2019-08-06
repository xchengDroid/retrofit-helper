package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.Request;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 */
@SuppressWarnings("JavadocReference")
public interface LifeCall<T> extends Observer<Lifecycle.Event> {

    String TAG = "LifeCall";

    /**
     * @param provider lifecycleProvider
     * @return result
     * @throws Throwable if is {@link DisposedException},the call is disposed.
     */
    @NonNull
    T execute(@Nullable LifecycleProvider provider) throws Throwable;

    void enqueue(@Nullable LifecycleProvider provider, LifeCallback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    LifeCall<T> clone();

    Request request();

    /**
     * Returns true if this call has been disposed.
     *
     * @return true if this call has been disposed
     */
    boolean isDisposed();
}
