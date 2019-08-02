package com.xcheng.retrofit;

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

    @NonNull
    Result<T> parseResponse(LifeCall<T> call2, Response<T> response);

    /**
     * 统一解析Throwable对象转换为HttpError对象。如果为HttpError，
     * 则为{@link retrofit2.Converter#convert(Object)}内抛出的异常
     *
     * @param call2 call
     * @param t     Throwable
     * @return HttpError result
     */
    @NonNull
    HttpError parseThrowable(LifeCall<T> call2, Throwable t);

    void onError(LifeCall<T> call, HttpError error);

    void onSuccess(LifeCall<T> call, T response);

    /**
     * @param t 请求失败的错误信息
     */
    void onCompleted(LifeCall<T> call, @Nullable Throwable t);

    /**
     * 由于生命周期原因请求被取消了
     */
    void onDisposed(LifeCall<T> call);
}