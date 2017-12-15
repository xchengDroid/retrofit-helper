package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 通用请求 支持 GET POST HEAD DELETE PUT PATCH 中的任意类型,
 * 自由构造RequestBody
 * Created by chengxin on 2017/6/22.
 */
public class AnyRequest extends OkRequest {

    private final RequestBody requestBody;

    private AnyRequest(Builder builder) {
        super(builder);
        this.requestBody = builder.requestBody;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(url()).tag(tag()).headers(headers()).method(method(), requestBody).build();
    }


    public static class Builder extends OkRequest.Builder<Builder> {
        private RequestBody requestBody;

        /**
         * @see RequestBody#create(MediaType, String)
         */
        public Builder requestBody(@Nullable MediaType mediaType, String content) {
            return requestBody(RequestBody.create(mediaType, content));
        }

        public Builder requestBody(RequestBody requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        @Override
        public Builder param(String key, String value) {
            throw new UnsupportedOperationException("Unsupported for AnyRequest");
        }

        @Override
        public AnyRequest build() {
            return new AnyRequest(this);
        }
    }
}
