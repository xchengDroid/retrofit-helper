package com.xcheng.retrofit;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * 监听下载进度 {@link Callback}
 *
 * @param <T> Successful response body type.
 */
public interface ResponseBodyCallback<T> extends Callback<ResponseBody> {

    T convert(Call<ResponseBody> call, ResponseBody value) throws IOException;

    void onProgress(Call<ResponseBody> call, long progress, long contentLength, boolean done);
}