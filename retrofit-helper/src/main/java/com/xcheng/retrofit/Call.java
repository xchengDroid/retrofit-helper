package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import okhttp3.Request;
import retrofit2.Response;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 *
 * @param <T> Successful response body type.
 */
public interface Call<T> extends Cloneable {

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

    Request request();

    /**
     * Synchronously send the request and return its response body.
     *
     * @throws retrofit2.HttpException if {@link Response#body()} is null
     * @throws java.io.IOException     if a problem occurred talking to the server.
     * @throws RuntimeException        (and subclasses) if an unexpected error occurs creating the request
     *                                 or decoding the response.
     */
    @NonNull
    T execute() throws Throwable;

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(Callback<T> callback);

}
