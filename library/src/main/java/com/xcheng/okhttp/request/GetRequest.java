package com.xcheng.okhttp.request;

import android.net.Uri;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 通用的GET请求
 * Created by chengxin on 2017/6/22.
 */
public class GetRequest extends OkRequest {
    private final HttpUrl paramUrl;

    private GetRequest(Builder builder) {
        super(builder);
        HttpUrl.Builder urlBuilder = url().newBuilder();
        final boolean encoded = builder.encoded;
        for (Map.Entry<String, String> entry : params().entrySet()) {
            String value = encoded ? entry.getValue() : Uri.encode(entry.getValue()/* ' ' space encoded %20 */);
            urlBuilder.addEncodedQueryParameter(entry.getKey(), value);
        }
        this.paramUrl = urlBuilder.build();
    }

    public HttpUrl paramUrl() {
        return paramUrl;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(paramUrl).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        //是否已经编码过了
        private boolean encoded = false;

        public Builder encoded(boolean encoded) {
            this.encoded = encoded;
            return this;
        }

        @Override
        public GetRequest build() {
            //忽略method设置
            method(OkRequest.GET);
            return new GetRequest(this);
        }
    }
}
