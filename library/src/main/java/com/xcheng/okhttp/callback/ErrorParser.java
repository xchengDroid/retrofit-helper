package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.OkCall;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 实现 {@link #parseException(OkCall, IOException)}方法
 *
 * @param <T>
 */
public abstract class ErrorParser<T> implements HttpParser<T> {

    @Nullable
    @Override
    public OkResponse<T> mockResponse(OkCall<T> okCall) {
        return null;
    }

    @Override
    @NonNull
    public EasyError parseException(OkCall<T> okCall, IOException e) {
        String message = null;
        if (e instanceof UnknownHostException) {
            message = "网络异常";
        } else if (e instanceof ConnectException) {
            message = "网络异常";
        } else if (e instanceof SocketException) {
            message = "服务异常";
        } else if (e instanceof SocketTimeoutException) {
            message = "请求超时";
        }
        return new EasyError(message == null ? "请求失败" : message, e);
    }
}