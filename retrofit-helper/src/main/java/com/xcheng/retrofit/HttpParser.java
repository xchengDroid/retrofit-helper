package com.xcheng.retrofit;

import android.support.annotation.NonNull;

import retrofit2.Response;

public interface HttpParser<T> {

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

}