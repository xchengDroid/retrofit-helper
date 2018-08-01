package com.xcheng.retrofit.progress;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：拦截器实现监听进度
 */
public class ProgressInterceptor implements Interceptor {
    public static ProgressInterceptor INSTANCE = new ProgressInterceptor();
    public static final String KEY_HEADER_PROGRESS = "HeaderProgress";

    private ProgressInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String tag = request.header(KEY_HEADER_PROGRESS);
        //先判断是否有进度需求
        if (tag == null)
            return chain.proceed(request);

        RequestBody requestBody = request.body();
        //判断是否有上传需求
        if (requestBody != null) {
            List<ProgressListener> upListeners = ProgressManager.getInstance().getListeners(tag, false);
            if (upListeners.size() != 0) {
                Request.Builder builder = request.newBuilder();
                RequestBody newRequestBody = new ProgressRequestBody(requestBody, upListeners);
                request = builder.method(request.method(), newRequestBody).build();
            }
        }
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            List<ProgressListener> downListeners = ProgressManager.getInstance().getListeners(tag, true);
            Response.Builder builder = response.newBuilder();
            ResponseBody newResponseBody = new ProgressResponseBody(responseBody, downListeners);
            response = builder.body(newResponseBody).build();
        }
        return response;
    }
}
