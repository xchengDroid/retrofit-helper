package com.xcheng.okhttp.request;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.ParamUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 构造OkHttp请求相关参数
 * Created by cx on 17/6/22.
 */
public abstract class OkRequest {
    public static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    @StringDef({GET, POST, HEAD, DELETE, PUT, PATCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Method {
    }

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String HEAD = "HEAD";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";

    private final String method;
    private final HttpUrl url;
    private final Object tag;
    private final int id;
    private final Map<String, String> params;
    private final Headers headers;
    private final boolean inProgress;
    private final boolean outProgress;

    //发起请求 解析相关
    private final OkHttpClient client;
    //额外入参
    private final Map<String, Object> extraMap;
    private HttpParser.Factory factory;

    protected OkRequest(Builder<?> builder) {
        //build方法是抽象，本来应该在build方法里面做检测，现在放到构造函数里面统一检测
        EasyPreconditions.checkState(builder.url != null, "url==null");
        if (EasyOkHttp.okConfig().mustTag()) {
            EasyPreconditions.checkState(builder.tag != null, "tag==null");
        }
        //如果没有设置 默认为POST
        this.method = ParamUtil.defValueIfNull(builder.method, POST);
        this.url = builder.url;
        this.tag = ParamUtil.defValueIfNull(builder.tag, this);
        this.params = builder.params;
        this.headers = builder.headers.build();
        this.id = builder.id;
        this.inProgress = builder.inProgress;
        this.outProgress = builder.outProgress;
        this.client = ParamUtil.defValueIfNull(builder.client, EasyOkHttp.okConfig().client());
        this.extraMap = builder.extraMap;
        this.factory = ParamUtil.defValueIfNull(builder.factory, EasyOkHttp.okConfig().parserFactory());
    }

    public String method() {
        return method;
    }

    public HttpUrl url() {
        return url;
    }

    public OkHttpClient client() {
        return client;
    }

    @Nullable
    @CheckResult
    @SuppressWarnings("unchecked")
    public <E> E extra(String key) {
        if (extraMap != null) {
            return (E) extraMap.get(key);
        }
        return null;
    }

    @Nullable
    public Map<String, Object> extraMap() {
        return extraMap;
    }

    public HttpParser.Factory parserFactory() {
        return factory;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{method="
                + method
                + ",url="
                + url
                + ", tag="
                + (tag != this ? tag : null)
                + ", id="
                + id
                + '}';
    }

    public abstract Request createRequest();

    /**
     * Created by cx on 17/6/22.
     */
    @SuppressWarnings("unchecked")
    protected static abstract class Builder<T extends Builder> {
        private String method;
        private HttpUrl url;
        private Object tag;
        private Headers.Builder headers;

        /**
         * 保存http请求String类型参数，如Get请求查询参数，form表单等，根据需要子类自己实现
         */
        private final Map<String, String> params = new LinkedHashMap<>();
        private int id;
        private boolean inProgress;
        private boolean outProgress;

        //发起请求 解析相关
        private OkHttpClient client;
        private Map<String, Object> extraMap;
        private HttpParser.Factory factory;

        public Builder() {
            this.headers = new Headers.Builder();
            this.inProgress = false;
            this.outProgress = false;
        }

        public T method(@Method String method) {
            EasyPreconditions.checkNotNull(method, "method==null");
            this.method = method;
            return (T) this;
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

        /**
         * 设置Http请求的判断url地址，see {@link HttpUrl#resolve(String)}
         *
         * @param url http请求的url
         */
        public T url(String url) {
            EasyPreconditions.checkNotNull(url, "url==null");
            HttpUrl baseUrl = EasyOkHttp.okConfig().baseUrl();
            HttpUrl resolveUrl = baseUrl.resolve(url);
            if (resolveUrl == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", url: " + url);
            }
            this.url = resolveUrl;
            return (T) this;
        }


        /**
         * @param tag 标记该Http请求，调用cancel时传入的参数
         */
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
            EasyPreconditions.checkNotNull(key, "key==null");
            params.put(key, value);
            return (T) this;
        }

        /**
         * @param key   param 中的 key
         * @param value 调用 {@link String#valueOf(Object)}转换成 String对象
         * @return Builder
         */
        public T param(String key, Object value) {
            String result = null;
            if (value != null) {
                result = String.valueOf(value);
            }
            return param(key, result);
        }

        public T client(OkHttpClient client) {
            this.client = client;
            return (T) this;
        }

        public T parserFactory(HttpParser.Factory factory) {
            this.factory = factory;
            return (T) this;
        }

        /**
         * 约定大于配置，在{@link HttpParser#parseNetworkResponse(OkCall, Response)}方法中获取需要的的参数解析即可。
         * Http 响应解析所需要的额外数据 eg：gson解析需要的{@link java.lang.reflect.Type},
         * 下载文件保存的地址path，某些返回需要检测Header信息session权限等
         *
         * @param key   Map key
         * @param value Map value
         */
        public T extra(String key, Object value) {
            //Lazy Initialization
            if (this.extraMap == null) {
                this.extraMap = new LinkedHashMap<>();
            }
            this.extraMap.put(key, value);
            return (T) this;
        }

        public abstract OkRequest build();
    }
}
