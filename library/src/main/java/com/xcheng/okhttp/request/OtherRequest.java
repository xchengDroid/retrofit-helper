package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.xcheng.okhttp.util.EasyPreconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 通用请求 GET POST HEAD DELETE PUT PATCH
 * Created by chengxin on 2017/6/22.
 */
public class OtherRequest extends OkRequest {

    private RequestBody requestBody;
    private String method;

    private OtherRequest(Builder builder) {
        super(builder);
        this.method = builder.method;
        this.requestBody = builder.requestBody;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(url()).tag(tag()).headers(headers()).method(method, requestBody).build();
    }

    @StringDef({GET, POST, HEAD, DELETE, PUT, PATCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Method {
    }

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String HEAD = "HEAD";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";

    public static class Builder extends OkRequest.Builder<Builder> {
        private RequestBody requestBody;
        private String method;

        public Builder method(@Method String method) {
            this.method = method;
            return this;
        }

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
            throw new UnsupportedOperationException("Unsupported for OtherRequest");
        }

        @Override
        public OtherRequest build() {
            EasyPreconditions.checkState(method != null, "method==null");
            return new OtherRequest(this);
        }
    }
}
