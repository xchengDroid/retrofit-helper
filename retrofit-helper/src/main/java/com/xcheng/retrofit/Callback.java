package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import retrofit2.Response;

/**
 * if {@link LifeCall#isDisposed()} return true, all methods will not call
 *
 * @param <T> Successful response body type.
 */
@UiThread
public interface Callback<T> {
    /**
     * @param call The {@code Call} that was started
     */
    void onStart(Call<T> call);

    /**
     * @param call The {@code Call} that has thrown exception
     * @param t    统一解析throwable对象转换为HttpError对象，如果throwable为{@link HttpError}
     *             <li>则为{@link retrofit2.Converter#convert(Object)}内抛出的异常</li>
     *             如果为{@link retrofit2.HttpException}
     *             <li>则为{@link Response#body()}为null的时候抛出的</li>
     */
    @NonNull
    HttpError parseThrowable(Call<T> call, Throwable t);

    /**
     * 过滤一次数据,如剔除List中的null等,默认可以返回t
     */
    @NonNull
    T transform(Call<T> call, T t);

    void onError(Call<T> call, HttpError error);

    void onSuccess(Call<T> call, T t);

    /**
     * @param t 请求失败的错误信息
     */
    void onCompleted(Call<T> call, @Nullable Throwable t);
}