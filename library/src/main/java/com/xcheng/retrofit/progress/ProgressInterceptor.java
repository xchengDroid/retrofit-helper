package com.xcheng.retrofit.progress;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;

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
    //标记头部上传
    private static final String KEY_UPLOAD = "Content-Upload";
    //标记头部下载
    private static final String KEY_DOWNLOAD = "Content-Download";
    private final ProgressListener upListener;
    private final ProgressListener downListener;
    private final Executor callbackExecutor;

    /**
     * @param upListener       上传进度
     * @param downListener     下载进度
     * @param callbackExecutor
     */
    public ProgressInterceptor(@Nullable ProgressListener upListener, @Nullable ProgressListener downListener, @Nullable Executor callbackExecutor) {
        this.upListener = upListener;
        this.downListener = downListener;
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        if (upListener != null && requestBody != null) {
            Request.Builder builder = request.newBuilder();
            RequestBody newRequestBody = new ProgressRequestBody(requestBody, wrapListener(upListener));
            request = builder.method(request.method(), newRequestBody).build();
        }
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (downListener != null && responseBody != null) {
            Response.Builder builder = response.newBuilder();
            ResponseBody newResponseBody = new ProgressResponseBody(responseBody, wrapListener(downListener));
            response = builder.body(newResponseBody).build();
        }
        return response;
    }

    private ProgressListener wrapListener(final ProgressListener progressListener) {
        if (callbackExecutor == null) {
            return progressListener;
        }
        return new ProgressListener() {
            @Override
            public void onProgress(final long progress, final long contentLength, final boolean done) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        progressListener.onProgress(progress, contentLength, done);
                    }
                });
            }
        };
    }
}
