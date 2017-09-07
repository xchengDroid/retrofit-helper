package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.xcheng.okhttp.util.EasyPreconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.http.HttpMethod;

/**
 * 提交字text,json等
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

    public String getMethod() {
        return method;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    @Override
    public Request createRequest() {
        Request.Builder builder = new Request.Builder().url(url()).tag(tag()).headers(headers());
        switch (method) {
            case DELETE:
                if (requestBody == null) {
                    return builder.delete().build();
                } else {
                    return builder.delete(requestBody).build();
                }
            case HEAD:
                return builder.head().build();
            case PUT:
            case PATCH:
                return builder.patch(requestBody).build();
            default:
                throw new IllegalStateException("UnSupport method:" + method);
        }
    }

    @StringDef({HEAD, DELETE, PUT, PATCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Method {
    }

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
            requestBody(RequestBody.create(mediaType, content));
            return this;
        }

        public Builder requestBody(RequestBody requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        @Override
        public OtherRequest build() {
            EasyPreconditions.checkState(method != null, "method==null");
            if (requestBody != null && !HttpMethod.permitsRequestBody(method)) {
                EasyPreconditions.checkState(false, "method " + method + " must not have a request body.");
            }
            if (requestBody == null && HttpMethod.requiresRequestBody(method)) {
                EasyPreconditions.checkState(false, "method " + method + " must have a request body.");
            }
            return new OtherRequest(this);
        }
    }
}
