package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.JsonParser;
import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.util.EasyPreconditions;

import okhttp3.OkHttpClient;

/**
 * 全局的EasyOkHttp配置类
 * Created by chengxin on 2017/6/27.
 */
public class OkConfig {
    private final OkHttpClient okHttpClient;
    private final String host;
    private final Class<? extends HttpParser> parseClass;
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

    public OkHttpClient okHttpClient() {
        return okHttpClient;
    }

    public String host() {
        EasyPreconditions.checkState(host != null, "host==null,have you init it in OkConfig");
        return host;
    }

    public boolean postUiIfCanceled() {
        return postUiIfCanceled;
    }

    public Class<? extends HttpParser> parseClass() {
        return parseClass;
    }

    public static class Builder {
        private OkHttpClient okHttpClient;
        private String host;
        private boolean postUiIfCanceled;
        private Class<? extends HttpParser> parseClass;

        public Builder() {
            //default
            okHttpClient = new OkHttpClient();
            postUiIfCanceled = false;
            parseClass = JsonParser.class;
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

        public Builder parseClass(Class<? extends HttpParser> parseClass) {
            this.parseClass = EasyPreconditions.checkNotNull(parseClass, "parseClass==null");
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }
    }
}
