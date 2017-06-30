package com.xc.okhttp;


import android.util.Log;

import com.xc.okhttp.request.GetRequest;
import com.xc.okhttp.request.OkConfig;
import com.xc.okhttp.request.PostFormRequest;
import com.xc.okhttp.request.PostStrRequest;
import com.xc.okhttp.utils.OkExceptions;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by chengxin on 17/6/22
 */
public class EasyOkHttp {

    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private static OkConfig sOkConfig;
    public static final String TAG = EasyOkHttp.class.getSimpleName();

    public synchronized static void init(OkConfig okConfig) {
        if (okConfig == null) {
            OkExceptions.illegalArgument("OkConfig  can not be initialized with null");
        }
        if (sOkConfig != null) {
            Log.e(TAG, "try to initialize OkConfig which had already been initialized before");
            return;
        }
        sOkConfig = okConfig;
    }

    public static OkConfig getOkConfig() {
        if (sOkConfig == null) {
            OkExceptions.illegalState("EasyOkHttp must be init with OkConfig before using");
        }
        return sOkConfig;
    }

    public static GetRequest.Builder get(String url) {
        return new GetRequest.Builder().url(url);
    }

    public static PostStrRequest.Builder postStr(String url) {
        return new PostStrRequest.Builder().url(url);
    }

    public static PostFormRequest.Builder postForm(String url) {
        return new PostFormRequest.Builder().url(url);
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

