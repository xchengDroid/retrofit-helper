package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.error.EasyError;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 实现 {@link #getError(IOException)}方法
 *
 * @param <T>
 */
public abstract class SimpleParse<T> implements ResponseParse<T> {

    @NonNull
    public EasyError getError(IOException e) {
        if (e instanceof UnknownHostException) {
            return new EasyError(-101, e.getMessage());
        } else if (e instanceof ConnectException) {
            return new EasyError(-102, e.getMessage());
        } else if (e instanceof SocketException) {
            return new EasyError(-103, e.getMessage());
        } else if (e instanceof SocketTimeoutException) {
            return new EasyError(-104, e.getMessage());
        }
        return new EasyError(-105, e.getMessage());
    }
}