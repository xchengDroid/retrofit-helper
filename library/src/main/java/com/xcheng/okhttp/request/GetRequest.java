package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 通用的GET请求
 * Created by chengxin on 2017/6/22.
 */
public class GetRequest extends OkRequest {
    private final HttpUrl queryUrl;

    private GetRequest(Builder builder) {
        super(builder);
        HttpUrl.Builder urlBuilder = url().newBuilder();
        final IQueryEncoder queryEncoder = builder.queryEncoder;
        for (Map.Entry<String, String> entry : params().entrySet()) {
            if (queryEncoder == null) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            } else {
                queryEncoder.addQuery(urlBuilder, entry.getKey(), entry.getValue());
            }
        }
        this.queryUrl = urlBuilder.build();
    }

    public HttpUrl queryUrl() {
        return queryUrl;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(queryUrl).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        private IQueryEncoder queryEncoder;

        /**
         * @param queryEncoder 自定义编码转换
         */
        public Builder queryEncoder(IQueryEncoder queryEncoder) {
            this.queryEncoder = queryEncoder;
            return this;
        }

        @Override
        public GetRequest build() {
            //忽略method设置
            method(OkRequest.GET);
            return new GetRequest(this);
        }
    }

    public interface IQueryEncoder {
        /**
         * call {@link HttpUrl.Builder#addQueryParameter(String, String)}
         * or {@link HttpUrl.Builder#addEncodedQueryParameter(String, String)}
         * to addQuery
         *
         * @param builder HttpUrl构造器
         * @param name    查询的名字
         * @param value   查询的参数值
         */
        void addQuery(HttpUrl.Builder builder, String name, @Nullable String value);
    }
}
