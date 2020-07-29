package com.xcheng.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 创建时间：2019-09-05
 * 编写人： chengxin
 * 功能描述： 替换修改{@link Request#url()}
 */
public abstract class ReplaceUrlCallFactory extends CallFactoryProxy {
    private static final String BASE_URL_NAME = "BaseUrlName";

    public ReplaceUrlCallFactory(@NonNull Call.Factory delegate) {
        super(delegate);
    }

    @Override
    public final Call newCall(Request request) {
        /*
         * @Headers("BaseUrlName:xxx") for method, or
         * method(@Header("BaseUrlName") String name) for parameter
         */
        String baseUrlName = request.header(BASE_URL_NAME);
        if (baseUrlName != null) {
            okhttp3.HttpUrl newHttpUrl = getNewUrl(baseUrlName, request);
            if (newHttpUrl != null) {
                Request newRequest = request.newBuilder().url(newHttpUrl).build();
                return delegate.newCall(newRequest);
            } else {
                Log.w(RetrofitFactory.TAG, "getNewUrl() return null when baseUrlName==" + baseUrlName);
            }
        }
        return delegate.newCall(request);
    }

    /**
     * @param baseUrlName name to sign baseUrl
     * @return new httpUrl, if null use old httpUrl
     */
    @Nullable
    protected abstract HttpUrl getNewUrl(String baseUrlName, Request request);

}