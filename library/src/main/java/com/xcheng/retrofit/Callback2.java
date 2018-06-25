package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

/**
 * @param <T> Successful response body type.
 */
public abstract class Callback2<T> {

    @WorkerThread
    @NonNull
    public Result<T> parseResponse(Call2<T> call2, Response<T> response) {
        T body = response.body();
        if (response.isSuccessful()) {
            if (body != null) {
                return Result.success(body);
            } else {
                return Result.error(new HttpError("暂无数据", response));
            }
        }

        final String msg;
        switch (response.code()) {
            case 400:
                msg = "参数错误";
                break;
            case 401:
                msg = "身份未授权";
                break;
            case 403:
                msg = "禁止访问";
                break;
            case 404:
                msg = "地址未找到";
                break;
            default:
                msg = "服务异常";
        }
        return Result.error(new HttpError(msg, response));
    }

    /**
     * 统一解析Throwable对象转换为HttpError对象
     *
     * @param call2 call
     * @param t     Throwable
     * @return HttpError result
     */
    @WorkerThread
    @NonNull
    public HttpError parseThrowable(Call2<T> call2, Throwable t) {
        if (t instanceof HttpError) {
            //用于convert函数直接抛出异常接收
            return (HttpError) t;
        } else if (t instanceof UnknownHostException) {
            return new HttpError("网络异常", t);
        } else if (t instanceof ConnectException) {
            return new HttpError("网络异常", t);
        } else if (t instanceof SocketException) {
            return new HttpError("服务异常", t);
        } else if (t instanceof SocketTimeoutException) {
            return new HttpError("响应超时", t);
        } else {
            return new HttpError("请求失败", t);
        }
    }

    public void onStart(Call2<T> call2) {
    }

    public void onCancel(Call2<T> call2) {
    }

    public abstract void onError(Call2<T> call2, HttpError error);

    public abstract void onSuccess(Call2<T> call2, T response);

    /**
     * 请求回调全部完成时执行
     *
     * @param call2 Call
     */
    public void onCompleted(Call2<T> call2) {
    }
}