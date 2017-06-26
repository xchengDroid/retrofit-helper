package com.xc.okhttp;


import com.xc.okhttp.callback.ResponseParse;
import com.xc.okhttp.request.GetRequest;
import com.xc.okhttp.request.PostStrRequest;
import com.xc.okhttp.utils.OkExceptions;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by zhy on 15/8/17.
 */
public class EasyOkHttp {

    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private static OkHttpClient sOkHttpClient;
    private static String sHost;
    private static Class<? extends ResponseParse> sResponseParseClass;
    private static boolean sPostUiIfCanceled;

    public static void init(OkHttpClient okHttpClient, String host, Class<? extends ResponseParse> responseParseClass, boolean postUiIfCanceled) {
        synchronized (EasyOkHttp.class) {
            if (sOkHttpClient != null) {
                return;
            }
            if (okHttpClient == null) {
                OkExceptions.illegalArgument("okHttpClient can not be null");
            } else if (host == null) {
                OkExceptions.illegalArgument("host can not be null");
            } else if (responseParseClass == null) {
                OkExceptions.illegalArgument("responseParseClass can not be null");
            }
            sOkHttpClient = okHttpClient;
            sHost = host;
            sResponseParseClass = responseParseClass;
            sPostUiIfCanceled = postUiIfCanceled;
        }
    }

    public static OkHttpClient getClient() {
        if (sOkHttpClient == null) {
            OkExceptions.illegalState("please init EasyOkHttp before call this method !");
        }
        return sOkHttpClient;
    }

    public static String getHost() {
        if (sHost == null) {
            OkExceptions.illegalState("please init EasyOkHttp before call this method !");
        }
        return sHost;
    }

    public static Class<? extends ResponseParse> getResponseParseClass() {
        if (sResponseParseClass == null) {
            OkExceptions.illegalState("please init EasyOkHttp before call this method !");
        }
        return sResponseParseClass;
    }

    /**
     * 当请求被取消的时候是否取消UI层的回调
     *
     * @return
     */
    public static boolean isPostUiIfCanceled() {
        return sPostUiIfCanceled;
    }

    public static GetRequest.Builder get(String url) {
        return new GetRequest.Builder().url(url);
    }

    public static PostStrRequest.Builder postStr(String url) {
        return new PostStrRequest.Builder().url(url);
    }

    //取消请求
    public static void cancel(Object tag) {
        for (Call call : sOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : sOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

}

