package com.xc.okhttp;


import com.xc.okhttp.request.GetRequest;
import com.xc.okhttp.request.OkConfig;
import com.xc.okhttp.request.PostStrRequest;
import com.xc.okhttp.utils.OkExceptions;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by zhy on 15/8/17.
 */
public class EasyOkHttp {

    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private static OkConfig sOkConfig;

    public static void init(OkConfig okConfig) {
        OkExceptions.checkNotNull(okConfig, "okConfig==null");
        sOkConfig = okConfig;
    }

    public static OkConfig getOkConfig() {
        if (sOkConfig == null) {
            OkExceptions.illegalState("OkConfig==null,please init EasyOkHttp before call this method !");
        }
        return sOkConfig;
    }

    public static GetRequest.Builder get(String url) {
        return new GetRequest.Builder().url(url);
    }

    public static PostStrRequest.Builder postStr(String url) {
        return new PostStrRequest.Builder().url(url);
    }

    //取消请求
    public static void cancel(Object tag) {
        OkHttpClient client = getOkConfig().getOkHttpClient();
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}

