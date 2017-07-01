package com.xc.okhttp.request;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.xc.okhttp.EasyOkHttp;
import com.xc.okhttp.callback.ResponseParse;
import com.xc.okhttp.utils.OkExceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by cx on 17/6/22.
 */
public abstract class OkRequest {
    private String url;
    private Object tag;
    private int id;
    private Map<String, String> params;
    private Headers.Builder headers;
    private boolean inProgress;
    //发起请求 解析相关
    private OkHttpClient okHttpClient;
    private Map<String, Object> extraMap;
    private TypeToken<?> typeToken;
    private Class<? extends ResponseParse> responseParse;

    protected OkRequest(OkRequestBuilder<?> builder) {
        if (builder.url == null) {
            OkExceptions.illegalArgument("url can not be null.");
        }
        this.url = builder.url;
        this.tag = builder.tag;
        this.params = builder.params;
        this.headers = builder.headers;
        this.id = builder.id;
        this.inProgress = builder.inProgress;
        this.okHttpClient = builder.okHttpClient;
        this.extraMap = builder.extraMap;
        this.typeToken = builder.typeToken;
        this.responseParse = builder.responseParse;
    }

    public String getUrl() {
        return url;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Map<String, Object> getExtraMap() {
        return extraMap;
    }

    public TypeToken<?> getTypeToken() {
        return typeToken;
    }

    public Class<? extends ResponseParse> getResponseParse() {
        return responseParse;
    }

    public Object getTag() {
        return tag;
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @NonNull
    public Headers.Builder getHeaders() {
        return headers;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    protected abstract Request createRequest();

    /**
     * Created by zhy on 15/12/14.
     */
    @SuppressWarnings("unchecked")
    public static abstract class OkRequestBuilder<T extends OkRequestBuilder> {
        private String url;
        private Object tag;
        private Headers.Builder headers;
        private Map<String, String> params;
        private int id;
        private boolean inProgress;

        //发起请求 解析相关
        private OkHttpClient okHttpClient;
        private Map<String, Object> extraMap;
        private TypeToken<?> typeToken;
        private Class<? extends ResponseParse> responseParse;

        public OkRequestBuilder() {
            headers = new Headers.Builder();
            inProgress = false;
        }

        public T id(int id) {
            this.id = id;
            return (T) this;
        }

        public T inProgress() {
            this.inProgress = true;
            return (T) this;
        }

        public T url(String url) {
            if (url == null) {
                OkExceptions.illegalArgument("url can not be null");
            }
            if (!url.startsWith("http")) {
                url = EasyOkHttp.getOkConfig().getHost() + url;
            }
            this.url = url;
            return (T) this;
        }


        public T tag(Object tag) {
            this.tag = tag;
            return (T) this;
        }

        public T headers(Headers headers) {
            this.headers = headers.newBuilder();
            return (T) this;
        }

        public T header(String key, String val) {
            headers.add(key, val);
            return (T) this;
        }

        public T params(Map<String, String> params) {
            this.params = params;
            return (T) this;
        }

        public T param(String key, String val) {
            if (this.params == null) {
                params = new LinkedHashMap<>();
            }
            params.put(key, val);
            return (T) this;
        }

        public T okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return (T) this;
        }

        public T responseParse(Class<? extends ResponseParse> responseParse) {
            if (responseParse == null) {
                OkExceptions.illegalArgument("responseParseClass can not be null");
            }
            this.responseParse = responseParse;
            return (T) this;
        }

        public T typeToken(TypeToken<?> typeToken) {
            this.typeToken = typeToken;
            return (T) this;
        }

        public T extra(String key, Object val) {
            if (this.extraMap == null) {
                extraMap = new LinkedHashMap<>();
            }
            extraMap.put(key, val);
            return (T) this;
        }

        protected Map<String, String> getParams() {
            return params;
        }

        protected String getUrl() {
            return url;
        }

        public abstract OkRequest build();
    }
}
