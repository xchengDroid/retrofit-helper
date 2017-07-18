package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.ResponseParse;
import com.xcheng.okhttp.utils.OkExceptions;

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
    private boolean outProgress;

    //发起请求 解析相关
    private OkHttpClient okHttpClient;
    private Map<String, Object> extraMap;
    private TypeToken<?> typeToken;
    private Class<? extends ResponseParse> parseClass;

    protected OkRequest(OkRequestBuilder<?> builder) {
        if (builder.url == null) {
            OkExceptions.illegalArgument("url can not be null.");
        }
        this.url = builder.url;
        this.tag = builder.tag != null ? builder.tag : this;
        this.params = builder.params;
        this.headers = builder.headers;
        this.id = builder.id;
        this.inProgress = builder.inProgress;
        this.outProgress = builder.outProgress;
        this.okHttpClient = builder.okHttpClient != null ? builder.okHttpClient : EasyOkHttp.getOkConfig().getOkHttpClient();
        this.extraMap = builder.extraMap;
        this.typeToken = builder.typeToken;
        this.parseClass = builder.parseClass != null ? builder.parseClass : EasyOkHttp.getOkConfig().getParseClass();
    }

    public String url() {
        return url;
    }

    public OkHttpClient okHttpClient() {
        return okHttpClient;
    }

    @SuppressWarnings("unchecked")
    public <V> V extra(String key) {
        if (extraMap != null) {
            return (V) extraMap.get(key);
        }
        return null;
    }

    public Map<String, Object> extraMap() {
        return extraMap;
    }

    public TypeToken<?> typeToken() {
        return typeToken;
    }

    public Class<? extends ResponseParse> parseClass() {
        return parseClass;
    }

    public Object tag() {
        return tag;
    }

    public int id() {
        return id;
    }

    public Map<String, String> params() {
        return params;
    }

    @NonNull
    public Headers.Builder headers() {
        return headers;
    }

    public boolean inProgress() {
        return inProgress;
    }

    public boolean outProgress() {
        return outProgress;
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
        private boolean outProgress;

        //发起请求 解析相关
        private OkHttpClient okHttpClient;
        private Map<String, Object> extraMap;
        private TypeToken<?> typeToken;
        private Class<? extends ResponseParse> parseClass;

        public OkRequestBuilder() {
            this.headers = new Headers.Builder();
            this.inProgress = false;
            this.outProgress = false;
        }

        public T id(int id) {
            this.id = id;
            return (T) this;
        }

        public T inProgress() {
            this.inProgress = true;
            return (T) this;
        }

        public T outProgress() {
            this.outProgress = true;
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

        public T header(String key, String value) {
            headers.add(key, value);
            return (T) this;
        }

        public T params(Map<String, String> params) {
            this.params = params;
            return (T) this;
        }

        public T param(String key, String value) {
            if (this.params == null) {
                params = new LinkedHashMap<>();
            }
            params.put(key, value);
            return (T) this;
        }

        /**
         * @param key   param 中的 key
         * @param value 转换成 String对象
         * @return Builder
         */
        public T param(String key, Object value) {
            String result = null;
            if (value != null) {
                result = String.valueOf(value);
            }
            return param(key, result);
        }

        public T okHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return (T) this;
        }

        public T parseClass(Class<? extends ResponseParse> parseClass) {
            if (parseClass == null) {
                OkExceptions.illegalArgument("responseParseClass can not be null");
            }
            this.parseClass = parseClass;
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
