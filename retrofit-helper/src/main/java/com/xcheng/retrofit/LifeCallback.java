package com.xcheng.retrofit;

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
@UiThread
public interface LifeCallback<T> extends HttpParser<T> {

    void onStart(LifeCall<T> call);

    void onError(LifeCall<T> call, HttpError error);

    void onSuccess(LifeCall<T> call, T response);

    /**
     * @param t 请求失败的错误信息
     */
    void onCompleted(LifeCall<T> call, @Nullable Throwable t);

    /**
     * 由于生命周期原因请求被取消了
     */
    void onLifecycle(LifeCall<T> call);
}