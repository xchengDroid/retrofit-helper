package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;


/**
 * http解析的工具 成功返回所需的对象
 */
public interface ResponseParse<T> {
    /**
     * @throws NullPointerException 如果返回值为空
     */
    @NonNull
    OkResponse<T> parseNetworkResponse(OkCall<T> okCall, Response response) throws IOException;

    /**
     * called by {@link okhttp3.Callback#onFailure(Call, IOException)}
     *
     * @param e IO错误
     * @return EasyError  对应IOException的error信息
     * @throws NullPointerException 如果返回值为空
     */
    @NonNull
    EasyError getError(IOException e);
}