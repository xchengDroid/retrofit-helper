package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.callback.JsonParser;
import com.xcheng.okhttp.util.EasyPreconditions;

import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * 全局的EasyOkHttp配置类,具备全局属性的字段
 * Created by chengxin on 2017/6/27.
 */
public class OkConfig {
    private final HttpUrl baseUrl;
    private final boolean postUiIfCanceled;
    private boolean mustTag;

    private final OkHttpClient client;
    private final HttpParser.Factory factory;

    public Builder newBuilder() {
        return new Builder(this);
    }

    private OkConfig(Builder builder) {
        baseUrl = builder.baseUrl;
        postUiIfCanceled = builder.postUiIfCanceled;
        mustTag = builder.mustTag;

        factory = builder.factory;
        client = builder.client;
    }

    public HttpUrl baseUrl() {
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
        private HttpUrl baseUrl;
        private boolean postUiIfCanceled;
        private boolean mustTag;

        private OkHttpClient client;
        private HttpParser.Factory factory;

        public Builder() {
            postUiIfCanceled = false;
            mustTag = false;
        }

        Builder(OkConfig okConfig) {
            baseUrl = okConfig.baseUrl;
            postUiIfCanceled = okConfig.postUiIfCanceled;
            mustTag = okConfig.mustTag;

            client = okConfig.client;
            factory = okConfig.factory;
        }

        public Builder baseUrl(String baseUrl) {
            EasyPreconditions.checkNotNull(baseUrl, "baseUrl==null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            List<String> pathSegments = httpUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = httpUrl;
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
            EasyPreconditions.checkState(baseUrl != null, "baseUrl==null");
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
