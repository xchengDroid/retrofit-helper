package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import retrofit2.Call;
import retrofit2.Response;

/**
 * if {@link Call#cancel()}called, {@link #onStart(LifeCall)}、{@link #parseResponse(LifeCall, Response)}
 * 、{@link #parseThrowable(LifeCall, Throwable)}、{@link #onSuccess(LifeCall, Object)}、
 * {@link #onError(LifeCall, HttpError)}will not be called
 *
 * @param <T> Successful response body type.
 */
@SuppressWarnings("JavadocReference")
@UiThread
public interface LifeCallback<T> {

    void onStart(LifeCall<T> call);

    /**
     * 统一解析Throwable对象转换为HttpError对象。
     * <li>如果为{@link HttpError}则为{@link retrofit2.Converter#convert(Object)}内抛出的异常</li>
     * <li>或者为{@link retrofit2.HttpException}当 {@code body==null}的时候{@link retrofit2.CompletableFutureCallAdapterFactory}</li>
     */
    @NonNull
    HttpError parseThrowable(LifeCall<T> call, Throwable t);


    void onError(LifeCall<T> call, HttpError error);

    void onSuccess(LifeCall<T> call, T response);

    /**
     * @param t 请求失败的错误信息
     */
    void onCompleted(LifeCall<T> call, @Nullable Throwable t);

    /**
     * 由于生命周期原因请求被取消了
     */
    void onDisposed(LifeCall<T> call, Lifecycle.Event event);
}