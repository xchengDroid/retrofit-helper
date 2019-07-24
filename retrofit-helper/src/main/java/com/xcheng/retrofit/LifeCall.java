package com.xcheng.retrofit;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Response;

/**
 * 创建时间：2019/7/24
 * 编写人： chengxin
 * 描述：可以绑定Fragment 和 Activity 生命周期的LifeCall
 */
public interface LifeCall<T> extends Cloneable {
    /**
     * Synchronously send the request and return its response.
     *
     * @throws IOException      if a problem occurred talking to the server.
     * @throws RuntimeException (and subclasses) if an unexpected error occurs creating the request
     *                          or decoding the response.
     */
    Response<T> execute() throws IOException;

    /**
     * bind life
     *
     * @return current instance
     */
    LifeCall<T> bindLifeCycle();

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(LifeCallback<T> callback);

    /**
     * Returns true if this call has been either {@linkplain #execute() executed} or {@linkplain
     * #enqueue(LifeCallback) enqueued}. It is an error to execute or enqueue a call more than once.
     */
    boolean isExecuted();

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not
     * yet been executed it never will be.
     */
    void cancel();

    /**
     * True if {@link #cancel()} was called.
     */
    boolean isCanceled();

    /**
     * Create a new, identical call to this one which can be enqueued or executed even if this call
     * has already been.
     */
    LifeCall<T> clone();

    /**
     * The original HTTP request.
     */
    Request request();
}
