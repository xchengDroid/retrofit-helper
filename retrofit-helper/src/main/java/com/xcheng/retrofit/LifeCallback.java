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

    void onStart(LifeCall<T> call2);

    void onError(LifeCall<T> call2, HttpError error);

    void onSuccess(LifeCall<T> call2, T response);

    /**
     * @param t        请求失败的错误信息
     * @param canceled 请求是否被取消了
     */
    void onCompleted(LifeCall<T> call2, @Nullable Throwable t, boolean canceled);
}