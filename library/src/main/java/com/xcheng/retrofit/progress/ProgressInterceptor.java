package com.xcheng.retrofit.progress;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 创建时间：2018/8/2
 * 编写人： chengxin
 * 功能描述：上传或下载进度监听拦截器
 */
public class ProgressInterceptor implements Interceptor {
    /**
     * 标记Header的key ,如果header上有此键值对，尝试监听进度
     */
    private static final String KEY_HTTP_PROGRESS = "Http-Progress";
    private final ProgressListener mProgressListener;

    public ProgressInterceptor(ProgressListener progressListener) {
        this.mProgressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final String tag = request.header(KEY_HTTP_PROGRESS);
        //先判断是否有进度需求
        if (tag == null)
            return chain.proceed(request);

        RequestBody requestBody = request.body();
        //判断是否有上传需求
        if (requestBody != null && requestBody.contentLength() > 0) {
            Request.Builder builder = request.newBuilder();
            RequestBody newRequestBody = new ProgressRequestBody(requestBody, mProgressListener, tag);
            request = builder.method(request.method(), newRequestBody).build();
        }

        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (responseBody != null && responseBody.contentLength() > 0) {
            Response.Builder builder = response.newBuilder();
            ResponseBody newResponseBody = new ProgressResponseBody(responseBody, mProgressListener, tag);
            response = builder.body(newResponseBody).build();
        }
        return response;
    }

}
