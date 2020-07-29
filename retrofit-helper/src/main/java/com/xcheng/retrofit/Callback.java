package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import retrofit2.Response;

/**
 * Callback methods are executed using the {@link retrofit2.Retrofit} callback executor. When none is
 * specified, the following defaults are used:
 * <ul>
 * <li>Android: Callbacks are executed on the application's main (UI) thread.</li>
 * <li>JVM: Callbacks are executed on the background thread which performed the request.</li>
 * </ul>
 * <p>
 * if {@link LifeCall#isDisposed()} return true, all methods will not call exclude {@link #onCompleted(Call, Throwable)}
 *
 * @param <T> Successful response body type.
 */
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
     * @param t 请求失败的错误信息，如果是 {@link DisposedException}代表请求被生命周期事件取消了，
     *          {@link LifeCall#isDisposed()} 返回true
     */
    void onCompleted(Call<T> call, @Nullable Throwable t);
}