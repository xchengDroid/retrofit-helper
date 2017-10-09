package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.ResponseParse;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.ParamUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by cx on 17/6/22.
 * 发起http请求内部的封装类
 */
final class RealCall<T> implements OkCall<T> {
    private static final List<RealCall<?>> ALL_CALLS = new ArrayList<>();

    private final OkRequest okRequest;
    //发起请求 解析相关
    private final ResponseParse<T> responseParse;
    private TypeToken<T> typeToken;
    private UICallback<T> executorCallback;

    private Call rawCall;
    private volatile boolean canceled;
    private boolean executed;

    @SuppressWarnings("unchecked")
    public RealCall(@NonNull OkRequest okRequest) {
        this.okRequest = okRequest;
        this.typeToken = (TypeToken<T>) okRequest.typeToken();
        this.responseParse = createResponseParse();
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
                    executorCallback.inProgress(RealCall.this, bytesWritten * 1.0f / contentLength, contentLength, done);

                }
            });
            builder.method(request.method(), requestBody);
            request = builder.build();
        }
        return okRequest.okHttpClient().newCall(request);
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
            return responseParse.parseNetworkResponse(this, rawCall.execute());
        } finally {
            finished(RealCall.this);
        }
    }

    @Override
    public void enqueue(UICallback<T> uiCallback) {
        this.executorCallback = uiCallback;
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            rawCall = createRawCall();
        }
        addCall(this);
        this.executorCallback.onBefore(this);
        if (canceled) {
            rawCall.cancel();
        }
        rawCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callFailure(responseParse.getError(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    response = wrapResponse(response);
                    OkResponse<T> okResponse = responseParse.parseNetworkResponse(RealCall.this, response);
                    EasyPreconditions.checkNotNull(okResponse, "okResponse==null");
                    if (okResponse.isSuccess()) {
                        callSuccess(okResponse.getBody());
                        return;
                    }
                    callFailure(okResponse.getError());
                } catch (IOException e) {
                    e.printStackTrace();
                    callFailure(responseParse.getError(e));
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
                    executorCallback.outProgress(RealCall.this, bytesRead * 1.0f / contentLength, contentLength, done);
                }
            });
            response = response.newBuilder()
                    .body(wrapBody).build();
        }
        return response;
    }

    @Override
    public TypeToken<T> getTypeToken() {
        return typeToken;
    }

    void setTokenIfNull(@NonNull Class<? extends UICallback> tokenClazz) {
        if (typeToken == null) {
            typeToken = ParamUtil.createTypeToken(tokenClazz);
        }
    }

    @Override
    public boolean isPostUi() {
        return !isCanceled() || EasyOkHttp.getOkConfig().isPostUiIfCanceled();
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
        return new RealCall<>(okRequest);
    }

    static private class InstantiationException extends RuntimeException {
        private InstantiationException(String msg, Exception cause) {
            super(msg, cause);
        }
    }

    private ResponseParse<T> createResponseParse() {
        try {
            return okRequest.parseClass().newInstance();
        } catch (java.lang.InstantiationException e) {
            throw new InstantiationException("Unable to instantiate ResponseParse " + okRequest.parseClass().getName()
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to instantiate ResponseParse " + okRequest.parseClass().getName()
                    + ": make sure class name exists, is public, and has an"
                    + " empty constructor that is public", e);
        }
    }

    private static synchronized void addCall(RealCall<?> call) {
        ALL_CALLS.add(call);
    }

    static synchronized void finished(RealCall<?> call) {
        ALL_CALLS.remove(call);
    }

    /**
     * @return a copy list call
     */
    static synchronized List<RealCall<?>> getCalls() {
        return ParamUtil.immutableList(ALL_CALLS);
    }
}
