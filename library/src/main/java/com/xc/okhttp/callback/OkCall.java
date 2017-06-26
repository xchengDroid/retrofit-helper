package com.xc.okhttp.callback;

import com.google.gson.reflect.TypeToken;
import com.xc.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Request;

public interface OkCall<T> extends Cloneable {

    OkResponse<T> execute() throws IOException;

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     */
    void enqueue(UICallback<T> uiCallback);

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
    OkCall<T> clone();

    /**
     * The original HTTP request.
     */
    Request request();

    <V> V getExtra(String key);

    TypeToken<T> getTypeToken();

    int getId();

    ResponseParse<T> getResponseParse();
}
