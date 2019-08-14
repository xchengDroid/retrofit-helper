package com.xcheng.retrofit;


import android.support.annotation.NonNull;

import retrofit2.Response;

/**
 * 创建时间：2019-08-08
 * 编写人： chengxin
 * 功能描述： 可同步和异步调用
 *
 * @param <T> Successful response body type.
 */
@SuppressWarnings("JavadocReference")
public interface Callable<T> {
    /**
     * Synchronously send the request and return its response body.
     *
     * @throws DisposedException       if {@link LifeCall} has been dispose
     * @throws retrofit2.HttpException if {@link Response#body()} is null
     * @throws java.io.IOException     if a problem occurred talking to the server.
     * @throws RuntimeException        (and subclasses) if an unexpected error occurs creating the request
     */
    @NonNull
    T execute() throws Throwable;

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(Callback<T> callback);
}
