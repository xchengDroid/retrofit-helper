package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.ResponseParse;
import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.ParamUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 构造OkHttp请求相关参数
 * Created by cx on 17/6/22.
 */
public abstract class OkRequest {
    private final String url;
    private final Object tag;
    private final int id;
    private final Map<String, String> params;
    private final Headers headers;
    private final boolean inProgress;
    private final boolean outProgress;

    //发起请求 解析相关
    private final OkHttpClient okHttpClient;
    private final Map<String, Object> extraMap;
    private final TypeToken<?> typeToken;
    private final Class<? extends ResponseParse> parseClass;

    protected OkRequest(Builder<?> builder) {
        //build方法是抽象，本来应该在build方法里面做检测，现在放到构造函数里面统一检测
        EasyPreconditions.checkState(builder.url != null, "url==null");
        this.url = builder.url;
        this.tag = ParamUtil.defValueIfNull(builder.tag, this);
        this.params = builder.params;
        this.headers = builder.headers.build();
        this.id = builder.id;
        this.inProgress = builder.inProgress;
        this.outProgress = builder.outProgress;
        this.okHttpClient = ParamUtil.defValueIfNull(builder.okHttpClient, EasyOkHttp.getOkConfig().getOkHttpClient());
        this.extraMap = builder.extraMap;
        this.typeToken = builder.typeToken;
        this.parseClass = ParamUtil.defValueIfNull(builder.parseClass, EasyOkHttp.getOkConfig().getParseClass());
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

    @Nullable
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

    @NonNull
    public Map<String, String> params() {
        return params;
    }

    @NonNull
    public Headers headers() {
        return headers;
    }

    public boolean inProgress() {
        return inProgress;
    }

    public boolean outProgress() {
        return outProgress;
    }

    public abstract Request createRequest();

    /**
     * Created by cx on 17/6/22.
     */
    @SuppressWarnings("unchecked")
    public static abstract class Builder<T extends Builder> {
        private String url;
        private Object tag;
        private Headers.Builder headers;
        private final Map<String, String> params = new LinkedHashMap<>();
        private int id;
        private boolean inProgress;
        private boolean outProgress;

        //发起请求 解析相关
        private OkHttpClient okHttpClient;
        private Map<String, Object> extraMap;
        private TypeToken<?> typeToken;
        private Class<? extends ResponseParse> parseClass;

        public Builder() {
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
            EasyPreconditions.checkNotNull(url, "url==null");
            if (!url.startsWith("http")) {
                String host = EasyOkHttp.getOkConfig().getHost();
                boolean hostStart = host.endsWith("/");
                boolean urlStart = url.startsWith("/");
                if (hostStart && urlStart) {
                    url = host + url.substring(1);
                } else if (!hostStart && !urlStart) {
                    url = host + "/" + url;
                } else {
                    url = host + url;
                }
            }
            this.url = url;
            return (T) this;
        }


        public T tag(Object tag) {
            this.tag = tag;
            return (T) this;
        }

        public T headers(Headers headers) {
            //如果为空 自行奔溃
            this.headers = headers.newBuilder();
            return (T) this;
        }

        public T header(String key, String value) {
            headers.add(key, value);
            return (T) this;
        }

        /**
         * Set a field with the specified value. If the field is not found, it is added. If the field is
         * found, the existing values are replaced.
         */
        public T setHeader(String key, String value) {
            headers.set(key, value);
            return (T) this;
        }

        public T params(Map<String, String> map) {
            if (!this.params.isEmpty()) {
                this.params.clear();
            }
            this.params.putAll(map);
            return (T) this;
        }

        public T param(String key, String value) {
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
            this.parseClass = parseClass;
            return (T) this;
        }

        public T typeToken(TypeToken<?> typeToken) {
            this.typeToken = typeToken;
            return (T) this;
        }

        public T extra(String key, Object value) {
            //Lazy Initialization
            if (this.extraMap == null) {
                this.extraMap = new LinkedHashMap<>();
            }
            this.extraMap.put(key, value);
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
