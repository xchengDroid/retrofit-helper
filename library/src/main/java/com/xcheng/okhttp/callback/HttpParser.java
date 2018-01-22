package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.OkCall;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * http 请求返回解析接口类，解析{@link Response}和{@link IOException}
 */
public interface HttpParser<T> {
    /**
     * called by {@link okhttp3.Callback#onResponse(Call, Response)}}
     *
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
    EasyError parseException(OkCall<T> okCall, IOException e);

    /**
     * 根据类型获取解析的HttpParser
     * Created by chengxin on 2018/1/22.
     */
    interface Factory {
        /**
         * @param type 需要被解析的类型,用于标识不通的返回的类型
         * @return 创建对应的 HttpParser
         */
        @NonNull
        HttpParser<?> parser(@Nullable String type);
    }
}