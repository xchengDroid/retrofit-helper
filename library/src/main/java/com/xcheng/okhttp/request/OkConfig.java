package com.xcheng.okhttp.request;

import com.xcheng.okhttp.callback.JsonParse;
import com.xcheng.okhttp.callback.ResponseParse;
import com.xcheng.okhttp.util.OkExceptions;

import okhttp3.OkHttpClient;

/**
 * Created by chengxin on 2017/6/27.
 */

public class OkConfig {
    private final OkHttpClient okHttpClient;
    private final String host;
    private final Class<? extends ResponseParse> parseClass;
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
        OkExceptions.checkState(host == null, "host==null,have you init it in OkConfig");
        return host;
    }

    public boolean isPostUiIfCanceled() {
        return postUiIfCanceled;
    }

    public Class<? extends ResponseParse> getParseClass() {
        return parseClass;
    }

    public static class Builder {
        private OkHttpClient okHttpClient;
        private String host;
        private boolean postUiIfCanceled;
        private Class<? extends ResponseParse> parseClass;

        public Builder() {
            okHttpClient = new OkHttpClient();
            postUiIfCanceled = false;
            parseClass = JsonParse.class;
        }

        public Builder host(String host) {
            this.host = OkExceptions.checkNotNull(host, "host==null");
            return this;
        }

        public Builder okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = OkExceptions.checkNotNull(okHttpClient, "okHttpClient==null");
            return this;
        }

        public Builder postUiIfCanceled(boolean postUiIfCanceled) {
            this.postUiIfCanceled = postUiIfCanceled;
            return this;
        }

        public Builder parseClass(Class<? extends ResponseParse> parseClass) {
            this.parseClass = OkExceptions.checkNotNull(parseClass, "parseClass==null");
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }
    }
}
