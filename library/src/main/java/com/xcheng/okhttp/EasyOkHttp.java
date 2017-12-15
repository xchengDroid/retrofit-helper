package com.xcheng.okhttp;


import android.util.Log;

import com.xcheng.okhttp.request.OkCall;
import com.xcheng.okhttp.request.ExecutorCall;
import com.xcheng.okhttp.request.GetRequest;
import com.xcheng.okhttp.request.OkConfig;
import com.xcheng.okhttp.request.AnyRequest;
import com.xcheng.okhttp.request.FormRequest;
import com.xcheng.okhttp.request.JsonRequest;
import com.xcheng.okhttp.util.EasyPreconditions;

/**
 * Created by chengxin on 17/6/22
 */
public class EasyOkHttp {
    public static final long DEFAULT_MILLISECONDS = 10_000L;
    private static OkConfig sOkConfig;
    public static final String TAG = EasyOkHttp.class.getSimpleName();

    //be private
    private EasyOkHttp() {
    }

    /**
     * init EasyOkHttp with okConfig ,like ImageLoader.getInstance.init(config)
     */
    public static void init(OkConfig okConfig) {
        synchronized (EasyOkHttp.class) {
            if (sOkConfig != null) {
                Log.e(TAG, "try to initialize OkConfig which had already been initialized before");
                return;
            }
            sOkConfig = EasyPreconditions.checkNotNull(okConfig, "okConfig==null");
        }
    }

    /**
     * like  JobManager.create(this) JobManager.instance()
     */
    public static OkConfig getOkConfig() {
        if (sOkConfig == null) {
            synchronized (EasyOkHttp.class) {
                EasyPreconditions.checkState(sOkConfig != null, "EasyOkHttp must be init with OkConfig before using");
            }
        }
        return sOkConfig;
    }

    public static GetRequest.Builder get(String url) {
        return new GetRequest.Builder().url(url);
    }

    /**
     * 提交json，默认为POST请求
     */
    public static JsonRequest.Builder json(String url) {
        return new JsonRequest.Builder().url(url);
    }

    /**
     * 提交Form，默认为POST请求
     */
    public static FormRequest.Builder form(String url) {
        return new FormRequest.Builder().url(url);
    }

    /**
     * 自定义构造任何请求，注意：一定要设置method，默认没有设置
     */
    public static AnyRequest.Builder any(String url) {
        return new AnyRequest.Builder().url(url);
    }

    public static void cancel(Object tag) {
        for (OkCall<?> okCall : ExecutorCall.getCalls()) {
            if (okCall.request().tag().equals(tag)) {
                okCall.cancel();
            }
        }
    }

    public static void cancelAll() {
        for (OkCall<?> okCall : ExecutorCall.getCalls()) {
            okCall.cancel();
        }
    }
}

