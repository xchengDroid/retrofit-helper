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
    private final Class<? extends HttpParser> parserClass;
    private final boolean postUiIfCanceled;

    public static Builder newBuilder() {
        return new Builder();
    }

    private OkConfig(Builder builder) {
        okHttpClient = builder.okHttpClient;
        host = builder.host;
        postUiIfCanceled = builder.postUiIfCanceled;
        parserClass = builder.parserClass;
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

    public Class<? extends HttpParser> parserClass() {
        return parserClass;
    }

    public static class Builder {
        private OkHttpClient okHttpClient;
        private String host;
        private boolean postUiIfCanceled;
        private Class<? extends HttpParser> parserClass;

        public Builder() {
            //default
            okHttpClient = new OkHttpClient();
            postUiIfCanceled = false;
            parserClass = JsonParser.class;
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

        public Builder parserClass(Class<? extends HttpParser> parserClass) {
            this.parserClass = EasyPreconditions.checkNotNull(parserClass, "parserClass==null");
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }
    }
}
