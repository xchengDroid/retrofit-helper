package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.JsonParse;
import com.xcheng.okhttp.callback.HttpParse;
import com.xcheng.okhttp.util.EasyPreconditions;

import okhttp3.OkHttpClient;

/**
 * 全局的EasyOkHttp配置类
 * Created by chengxin on 2017/6/27.
 */
public class OkConfig {
    private final OkHttpClient okHttpClient;
    private final String host;
    private final Class<? extends HttpParse> parseClass;
    private final boolean postUiIfCanceled;

    public static Builder newBuilder() {
        return new Builder();
    }

    private OkConfig(Builder builder) {
        okHttpClient = builder.okHttpClient;
        host = builder.host;
        postUiIfCanceled = builder.postUiIfCanceled;
        parseClass = builder.parseClass;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public String getHost() {
        EasyPreconditions.checkState(host != null, "host==null,have you init it in OkConfig");
        return host;
    }

    public boolean postUiIfCanceled() {
        return postUiIfCanceled;
    }

    public Class<? extends HttpParse> getParseClass() {
        return parseClass;
    }

    public static class Builder {
        private OkHttpClient okHttpClient;
        private String host;
        private boolean postUiIfCanceled;
        private Class<? extends HttpParse> parseClass;

        public Builder() {
            //default
            okHttpClient = new OkHttpClient();
            postUiIfCanceled = false;
            parseClass = JsonParse.class;
        }

        public Builder host(String host) {
            this.host = EasyPreconditions.checkNotNull(host, "host==null");
            return this;
        }

        public Builder okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = EasyPreconditions.checkNotNull(okHttpClient, "okHttpClient==null");
            return this;
        }

        public Builder postUiIfCanceled(boolean postUiIfCanceled) {
            this.postUiIfCanceled = postUiIfCanceled;
            return this;
        }

        public Builder parseClass(Class<? extends HttpParse> parseClass) {
            this.parseClass = EasyPreconditions.checkNotNull(parseClass, "parseClass==null");
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }
    }
}
