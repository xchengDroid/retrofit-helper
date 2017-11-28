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
    private final OkHttpClient client;
    private final String host;
    private final ParserFactory parserFactory;
    private final boolean postUiIfCanceled;

    public static Builder newBuilder() {
        return new Builder();
    }

    private OkConfig(Builder builder) {
        client = builder.client;
        host = builder.host;
        postUiIfCanceled = builder.postUiIfCanceled;
        parserFactory = builder.parserFactory;
    }

    public OkHttpClient client() {
        return client;
    }

    public String host() {
        EasyPreconditions.checkState(host != null, "host==null,have you init it in OkConfig");
        return host;
    }

    public boolean postUiIfCanceled() {
        return postUiIfCanceled;
    }

    /**
     * create a new {@link HttpParser} instance
     */
    public HttpParser<?> parser() {
        return parserFactory.create();
    }

    public static class Builder {
        private OkHttpClient client;
        private String host;
        private boolean postUiIfCanceled;
        private ParserFactory parserFactory;

        public Builder() {
            postUiIfCanceled = false;
        }

        public Builder host(String host) {
            this.host = EasyPreconditions.checkNotNull(host, "host==null");
            return this;
        }

        public Builder client(OkHttpClient client) {
            this.client = EasyPreconditions.checkNotNull(client, "client==null");
            return this;
        }

        public Builder postUiIfCanceled(boolean postUiIfCanceled) {
            this.postUiIfCanceled = postUiIfCanceled;
            return this;
        }

        public Builder parserFactory(ParserFactory parserFactory) {
            this.parserFactory = EasyPreconditions.checkNotNull(parserFactory, "parserClass==null");
            return this;
        }

        /**
         * Note: If  {@link #client(OkHttpClient)}  is not called a default {@link
         * OkHttpClient} will be created and used.
         * If  {@link #parserFactory(ParserFactory)}  is not called a default {@link JsonParser} will be used.
         */
        public OkConfig build() {
            if (client == null) {
                //Lazy Initialization,because it is weight
                client = new OkHttpClient();
            }
            if (parserFactory == null) {
                parserFactory = new ParserFactory() {
                    @NonNull
                    @Override
                    public HttpParser<?> create() {
                        return new JsonParser<>();
                    }
                };
            }
            return new OkConfig(this);
        }
    }

    public interface ParserFactory {
        @NonNull
        HttpParser<?> create();
    }
}
