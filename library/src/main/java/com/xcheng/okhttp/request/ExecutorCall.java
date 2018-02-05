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
 * {@link OkCall}实现类,用于发起http请求
 *
 * @param <T> Successful response body type.
 */
public final class ExecutorCall<T> implements OkCall<T> {
    //保存所有未结束的OkCall
    private static final List<OkCall<?>> ALL_CALLS = new ArrayList<>();

    private final OkRequest okRequest;
    //发起请求 解析相关
    private final HttpParser<T> httpParser;
    //用户当json解析的TypeToken未设置的情况下，利用class动态生成Type
    private Type type;
    private PostCallback<T> postCallback;

    private Call rawCall;
    private volatile boolean canceled;
    private boolean executed;

    public ExecutorCall(@NonNull OkRequest okRequest) {
        this.okRequest = okRequest;
        //noinspection unchecked
        this.httpParser = (HttpParser<T>) okRequest.parserFactory().parser(okRequest);
    }

    @Override
    public OkRequest request() {
        return okRequest;
    }

    private Call createRawCall() {
        Request request = okRequest.createRequest();
        RequestBody body = request.body();
        if (okRequest.inProgress() && body != null && postCallback != null) {
            Request.Builder builder = request.newBuilder();
            RequestBody requestBody = new ProgressRequestBody(body, new ProgressRequestBody.Listener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    postCallback.inProgress(ExecutorCall.this, bytesWritten * 1.0f / contentLength, contentLength, done);
                }
            });
            builder.method(request.method(), requestBody);
            request = builder.build();
        }
        return okRequest.client().newCall(request);
    }

    private void callOkResponse(OkResponse<T> okResponse) {
        EasyPreconditions.checkNotNull(okResponse, "okResponse==null");
        if (okResponse.isSuccess()) {
            postCallback.onSuccess(this, okResponse.body());
        } else {
            postCallback.onError(this, okResponse.error());
        }
        postCallback.onFinish(this);
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
            OkResponse<T> mockResponse = httpParser.mockResponse(this);
            if (mockResponse != null) {
                return mockResponse;
            }
            return httpParser.parseNetworkResponse(this, rawCall.execute());
        } finally {
            finished(ExecutorCall.this);
        }
    }

    @Override
    public void enqueue(UICallback<T> uiCallback) {
        EasyPreconditions.checkNotNull(uiCallback, "uiCallback==null");
        this.type = ParamUtil.getType(uiCallback.getClass());
         this.postCallback = new PostCallback<>(uiCallback, new PostCallback.OnFinishedListener() {
            @Override
            public void onFinished(OkCall<?> okCall) {
                //等uiCallback所有主线程回调函数执行完才将call从列表移除，
                //这样的目的是为了在回调函数执行完之前的任意时刻都能在主线程取消当前的请求
                finished(okCall);
            }
        });
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            rawCall = createRawCall();
        }
        addCall(this);
        postCallback.onStart(this);
        if (canceled) {
            rawCall.cancel();
        }

        OkResponse<T> mockResponse = httpParser.mockResponse(this);
        if (mockResponse != null) {
            callOkResponse(mockResponse);
            return;
        }
        rawCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                EasyError error = httpParser.parseException(ExecutorCall.this, e);
                OkResponse<T> okResponse = OkResponse.error(error);
                callOkResponse(okResponse);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                try {
                    response = wrapResponse(response);
                    OkResponse<T> okResponse = httpParser.parseNetworkResponse(ExecutorCall.this, response);
                    callOkResponse(okResponse);
                } catch (IOException e) {
                    onFailure(call, e);
                } finally {
                    response.body().close();
                }
            }
        });
    }

    private Response wrapResponse(Response response) {
        if (okRequest.outProgress() && postCallback != null) {
            ResponseBody wrapBody = new ProgressResponseBody(response.body(), new ProgressResponseBody.Listener() {
                @Override
                public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
                    postCallback.outProgress(ExecutorCall.this, bytesRead * 1.0f / contentLength, contentLength, done);
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
        return !isCanceled() || EasyOkHttp.okConfig().postUiIfCanceled();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;

        okhttp3.Call call;
        synchronized (this) {
            //防止访问的时候rawCall正在多线程写入赋值故加同步锁，类似单利模式
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
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
