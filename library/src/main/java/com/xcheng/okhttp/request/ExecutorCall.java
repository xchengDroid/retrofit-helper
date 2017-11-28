package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.ParamUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by cx on 17/6/22.
 * 发起http请求的封装类
 */
public final class ExecutorCall<T> implements OkCall<T> {
    //保存所有未结束的OkCall
    private static final List<OkCall<?>> ALL_CALLS = new ArrayList<>();

    private final OkRequest okRequest;
    //发起请求 解析相关
    private final HttpParser<T> httpParser;
    //用户当json解析的TypeToken未设置的情况下，利用class动态生成Type
    private Type type;
    private ExecutorCallback<T> executorCallback;

    private Call rawCall;
    private volatile boolean canceled;
    private boolean executed;

    public ExecutorCall(@NonNull OkRequest okRequest) {
        this.okRequest = okRequest;
        this.httpParser = createHttpParser();
    }

    @Override
    public OkRequest request() {
        return okRequest;
    }

    private Call createRawCall() {
        Request request = okRequest.createRequest();
        RequestBody body = request.body();
        if (okRequest.inProgress() && body != null && executorCallback != null) {
            Request.Builder builder = request.newBuilder();
            RequestBody requestBody = new ProgressRequestBody(body, new ProgressRequestBody.Listener() {

                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    executorCallback.inProgress(ExecutorCall.this, bytesWritten * 1.0f / contentLength, contentLength, done);

                }
            });
            builder.method(request.method(), requestBody);
            request = builder.build();
        }
        return okRequest.client().newCall(request);
    }

    private void callFailure(EasyError error) {
        EasyPreconditions.checkNotNull(error, "error==null");
        executorCallback.onError(this, error);
        executorCallback.onAfter(this);
    }

    private void callSuccess(T t) {
        executorCallback.onSuccess(this, t);
        executorCallback.onAfter(this);
    }

    @Override
    public OkResponse<T> execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            rawCall = createRawCall();
        }
        addCall(this);
        if (canceled) {
            rawCall.cancel();
        }
        try {
            return httpParser.parseNetworkResponse(this, rawCall.execute());
        } finally {
            finished(ExecutorCall.this);
        }
    }

    @Override
    public void enqueue(UICallback<T> uiCallback) {
        EasyPreconditions.checkNotNull(uiCallback, "uiCallback==null");
        this.type = ParamUtil.getType(uiCallback.getClass());
        this.executorCallback = new ExecutorCallback<>(uiCallback, new ExecutorCallback.OnExecutorListener() {
            @Override
            public void onAfter() {
                finished(ExecutorCall.this);
            }
        });
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            rawCall = createRawCall();
        }
        addCall(this);
        executorCallback.onBefore(this);
        if (canceled) {
            rawCall.cancel();
        }
        rawCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callFailure(httpParser.parseException(ExecutorCall.this, e));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                try {
                    response = wrapResponse(response);
                    OkResponse<T> okResponse = httpParser.parseNetworkResponse(ExecutorCall.this, response);
                    EasyPreconditions.checkNotNull(okResponse, "okResponse==null");
                    if (okResponse.isSuccess()) {
                        callSuccess(okResponse.getBody());
                        return;
                    }
                    callFailure(okResponse.getError());
                } catch (IOException e) {
                    onFailure(call, e);
                } finally {
                    response.body().close();
                }
            }
        });
    }

    private Response wrapResponse(Response response) {
        if (okRequest.outProgress() && executorCallback != null) {
            ResponseBody wrapBody = new ProgressResponseBody(response.body(), new ProgressResponseBody.Listener() {
                @Override
                public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                    executorCallback.outProgress(ExecutorCall.this, bytesRead * 1.0f / contentLength, contentLength, done);
                }
            });
            response = response.newBuilder()
                    .body(wrapBody).build();
        }
        return response;
    }


    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isPostUi() {
        return !isCanceled() || EasyOkHttp.getOkConfig().postUiIfCanceled();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;
        synchronized (this) {
            if (rawCall != null) {
                rawCall.cancel();
            }
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public OkCall<T> clone() {
        ExecutorCall<T> okCall = new ExecutorCall<>(okRequest);
        okCall.type = type;
        return okCall;
    }

    static private class InstantiationException extends RuntimeException {
        private InstantiationException(String msg, Exception cause) {
            super(msg, cause);
        }
    }

    private HttpParser<T> createHttpParser() {
        try {
            return okRequest.parserClass().newInstance();
        } catch (java.lang.InstantiationException e) {
            throw new InstantiationException("Unable to instantiate HttpParser " + okRequest.parserClass().getName()
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to instantiate HttpParser " + okRequest.parserClass().getName()
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        }
    }

    private static synchronized void addCall(OkCall<?> call) {
        ALL_CALLS.add(call);
    }

    private static synchronized void finished(OkCall<?> call) {
        ALL_CALLS.remove(call);
    }

    public static synchronized List<OkCall<?>> getCalls() {
        return ParamUtil.immutableList(ALL_CALLS);
    }
}
