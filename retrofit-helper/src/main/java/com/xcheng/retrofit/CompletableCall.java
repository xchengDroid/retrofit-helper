package com.xcheng.retrofit;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import java.util.Objects;

import retrofit2.Call;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 *
 * @param <T> Successful response body type.
 */
public interface CompletableCall<T> extends Cloneable {

    Call<T> delegate();

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(Callback<T> callback);

    /**
     * default event is {@link androidx.lifecycle.Lifecycle.Event#ON_DESTROY}
     *
     * @param callback 回调函数
     * @param owner    LifecycleOwner ,当owner当前的状态为{@link androidx.lifecycle.Lifecycle.State#DESTROYED}
     *                 不会调用任何回调函数
     */
    default void enqueue(@Nullable LifecycleOwner owner, Callback<T> callback) {
        Objects.requireNonNull(callback, "callback==null");
        enqueue(owner != null ? new LifecycleCallback<>(this, callback, owner) : callback);
    }

    /**
     * Create a new, identical httpQueue to this one which can be enqueued even if this httpQueue
     * has already been.
     */
    CompletableCall<T> clone();
}
