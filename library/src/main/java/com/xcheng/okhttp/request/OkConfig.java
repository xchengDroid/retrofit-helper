package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.callback.JsonParser;
import com.xcheng.okhttp.util.EasyPreconditions;

import okhttp3.OkHttpClient;

/**
 * 全局的EasyOkHttp配置类
 * Created by chengxin on 2017/6/27.
 */
public class OkConfig {
    private final String baseUrl;
    private final boolean postUiIfCanceled;
    private boolean mustTag;

    private final OkHttpClient client;
    private final HttpParser.Factory factory;

    public static Builder newBuilder() {
        return new Builder();
    }

    private OkConfig(Builder builder) {
        baseUrl = builder.baseUrl;
        postUiIfCanceled = builder.postUiIfCanceled;
        mustTag = builder.mustTag;

        factory = builder.factory;
        client = builder.client;
    }

    public String baseUrl() {
        EasyPreconditions.checkState(baseUrl != null, "baseUrl==null,have you init it in OkConfig");
        return baseUrl;
    }

    public boolean postUiIfCanceled() {
        return postUiIfCanceled;
    }

    public boolean mustTag() {
        return mustTag;
    }

    public OkHttpClient client() {
        return client;
    }

    public HttpParser.Factory parserFactory() {
        return factory;
    }

    public static class Builder {
        private String baseUrl;
        private boolean postUiIfCanceled;
        private boolean mustTag;

        private OkHttpClient client;
        private HttpParser.Factory factory;

        public Builder() {
            postUiIfCanceled = false;
            mustTag = false;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = EasyPreconditions.checkNotNull(baseUrl, "baseUrl==null");
            return this;
        }

        /**
         * @param postUiIfCanceled true 当请求被取消的时候仍然回调UI,false 不回调
         */
        public Builder postUiIfCanceled(boolean postUiIfCanceled) {
            this.postUiIfCanceled = postUiIfCanceled;
            return this;
        }

        /**
         * @param mustTag true EasyOkHttp请求必须设置tag 否则抛异常，false 可以不传tag
         */
        public Builder mustTag(boolean mustTag) {
            this.mustTag = mustTag;
            return this;
        }

        public Builder client(OkHttpClient client) {
            this.client = EasyPreconditions.checkNotNull(client, "client==null");
            return this;
        }

        public Builder parserFactory(HttpParser.Factory factory) {
            this.factory = EasyPreconditions.checkNotNull(factory, "factory==null");
            return this;
        }

        /**
         * Note: If  {@link #client(OkHttpClient)}  is not called a default {@link
         * OkHttpClient} will be created and used.
         * If  {@link #parserFactory(HttpParser.Factory)}}  is not called a default {@link JsonParser} will be used.
         */
        public OkConfig build() {
            if (client == null) {
                //Lazy Initialization,because it is weight
                client = new OkHttpClient();
            }
            if (factory == null) {
                factory = DEFAULT_FACTORY;
            }
            return new OkConfig(this);
        }
    }

    static final HttpParser.Factory DEFAULT_FACTORY = new HttpParser.Factory() {
        @NonNull
        @Override
        public HttpParser<?> parser(OkRequest request) {
            return JsonParser.INSTANCE;
        }
    };
}
