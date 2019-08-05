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

    /**
     * @param call LifeCall
     * @param t    统一解析throwable对象转换为HttpError对象，如果throwable为{@link HttpError}
     *             <li>则为{@link retrofit2.Converter#convert(Object)}内抛出的异常</li>
     *             <li>或者当{@link Response#body()}为null的时候，此时{@link HttpError#body}为{@code Response}对象</li>
     */
    @NonNull
    HttpError parseThrowable(LifeCall<T> call, Throwable t);

    /**
     * 过滤一次数据,如剔除List中的null等,默认可以返回t
     */
    @NonNull
    T transform(LifeCall<T> call, T t);

    void onError(LifeCall<T> call, HttpError error);

    void onSuccess(LifeCall<T> call, T t);

    /**
     * @param t 请求失败的错误信息
     */
    void onCompleted(LifeCall<T> call, @Nullable Throwable t);

    /**
     * 由于生命周期原因请求被取消了,此回调函数不是与生命周期函数同步的
     */
    void onDisposed(LifeCall<T> call);
}