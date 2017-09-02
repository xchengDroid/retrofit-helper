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
 * 发起http请求的封装类
 */
public final class OkHttpCall<T> implements OkCall<T> {
    private static final List<OkHttpCall<?>> ALL_CALLS = new ArrayList<>();

    private final OkRequest okRequest;
    //发起请求 解析相关
    private final ResponseParse<T> responseParse;
    private TypeToken<T> typeToken;
    private Class<? extends UICallback> tokenClass;
    private ExecutorCallback<T> executorCallback;

    private Call rawCall;
    private volatile boolean canceled;
    private boolean executed;

    @SuppressWarnings("unchecked")
    public OkHttpCall(@NonNull OkRequest okRequest) {
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
                    executorCallback.inProgress(OkHttpCall.this, bytesWritten * 1.0f / contentLength, contentLength, done);

                }
            });
            builder.method(request.method(), requestBody);
            request = builder.build();
        }
        return okRequest.okHttpClient().newCall(request);
    }

    private void callFailure(EasyError error) {
        if (error == null) {
            error = EasyError.createDefaultError("do not find defined error in " + okRequest.parseClass() + ".getError(IOException) method");
        }
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
            finished(OkHttpCall.this);
        }
    }

    @Override
    public void enqueue(UICallback<T> uiCallback) {
        EasyPreconditions.checkNotNull(uiCallback, "uiCallback can not be null");
        this.tokenClass = uiCallback.getClass();
        this.executorCallback = new ExecutorCallback<>(uiCallback, new ExecutorCallback.OnExecutorListener() {
            @Override
            public void onAfter() {
                finished(OkHttpCall.this);
            }

            @NonNull
            @Override
            public EasyError canceledError() {
                String errorMsg = "Call Canceled, url= " + request().url();
                EasyError error = responseParse.getError(new IOException(errorMsg));
                if (error == null) {
                    error = EasyError.createDefaultError(errorMsg);
                }
                return error;
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
                callFailure(responseParse.getError(e));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                try {
                    response = wrapResponse(response);
                    OkResponse<T> okResponse = responseParse.parseNetworkResponse(OkHttpCall.this, response);
                    EasyError responseError = null;
                    if (okResponse != null) {
                        if (okResponse.isSuccess()) {
                            callSuccess(okResponse.getBody());
                            return;
                        }
                        responseError = okResponse.getError();
                    }
                    if (responseError == null) {
                        responseError = EasyError.createDefaultError("do not find error in " + okRequest.parseClass() + ".parseNetworkResponse(OkCall<T> , Response) , have you return it ?");
                    }
                    callFailure(responseError);
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
                    executorCallback.outProgress(OkHttpCall.this, bytesRead * 1.0f / contentLength, contentLength, done);
                }
            });
            response = response.newBuilder()
                    .body(wrapBody).build();
        }
        return response;
    }

    @Override
    public TypeToken<T> getTypeToken() {
        if (typeToken == null && tokenClass != null) {
            typeToken = ParamUtil.createTypeToken(tokenClass);
        }
        return typeToken;
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
        OkHttpCall<T> okCall = new OkHttpCall<>(okRequest);
        okCall.typeToken = getTypeToken();
        return okCall;
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


    private static synchronized void addCall(OkHttpCall<?> call) {
        ALL_CALLS.add(call);
    }

    private static synchronized void finished(OkHttpCall<?> call) {
        ALL_CALLS.remove(call);
    }

    public static synchronized List<OkHttpCall<?>> getCalls() {
        return ParamUtil.immutableList(ALL_CALLS);
    }
}
