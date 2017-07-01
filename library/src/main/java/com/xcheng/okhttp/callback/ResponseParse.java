package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Call;
import okhttp3.Response;


/**
 * http解析的工具 成功返回所需的对象
 **/
public abstract class ResponseParse<T> {

    @WorkerThread
    @NonNull
    public abstract OkResponse<T> parseNetworkResponse(OkCall<T> okCall, Response response, int id) throws IOException;

    /**
     * called by {@link okhttp3.Callback#onFailure(Call, IOException)}
     *
     * @param e IO错误
     * @return BaseError  对应IOException的error信息
     */
    @WorkerThread
    public BaseError getError(IOException e) {
        if (e instanceof UnknownHostException) {
            return new BaseError(-101, e.getMessage());
        } else if (e instanceof ConnectException) {
            return new BaseError(-102, e.getMessage());
        } else if (e instanceof SocketException) {
            return new BaseError(-103, e.getMessage());
        } else if (e instanceof SocketTimeoutException) {
            return new BaseError(-104, e.getMessage());
        }
        return new BaseError(-105, e.getMessage());
    }
}