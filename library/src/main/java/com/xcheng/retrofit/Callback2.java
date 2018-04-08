package com.xcheng.retrofit;

import android.support.annotation.NonNull;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

/**
 * @param <T>
 */
public abstract class Callback2<T> {

    @NonNull
    public Result<T> parseResponse(Call2<T> call2, Response<T> response) {
        T body = response.body();
        if (response.isSuccessful()) {
            if (body != null) {
                return Result.success(body);
            } else {
                return Result.error(new HttpError("暂无数据"));
            }
        }

        HttpError httpError;
        int httpCode = response.code();
        if (httpCode == 404) {
            httpError = new HttpError("地址未找到");
        } else if (httpCode == 400) {
            httpError = new HttpError("参数错误");
        } else {
            httpError = new HttpError("请求失败");
        }
        return Result.error(httpError);
    }

    @NonNull
    public Result<T> parseThrowable(Call2<T> call2, Throwable t) {
        HttpError httpError;
        if (t instanceof HttpError) {
            //用于convert函数直接抛出异常接收
            httpError = (HttpError) t;
        } else if (t instanceof UnknownHostException) {
            httpError = new HttpError("网络异常", t);
        } else if (t instanceof ConnectException) {
            httpError = new HttpError("网络异常", t);
        } else if (t instanceof SocketException) {
            httpError = new HttpError("服务异常", t);
        } else if (t instanceof SocketTimeoutException) {
            httpError = new HttpError("请求超时", t);
        } else {
            httpError = new HttpError("请求失败", t);
        }
        return Result.error(httpError);
    }

    public void onStart(Call2<T> call2) {
    }

    public void onCancel(Call2<T> call2) {
    }

    public abstract void onError(Call2<T> call2, HttpError error);

    public abstract void onSuccess(Call2<T> call2, T response);

    public void onFinish(Call2<T> call2) {
    }

}