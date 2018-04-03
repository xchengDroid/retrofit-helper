package com.xcheng.okhttp.request;

import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.util.ParamUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

import static com.xcheng.okhttp.util.EasyPreconditions.checkNotNull;
import static com.xcheng.okhttp.util.EasyPreconditions.checkState;

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
    private final Map<String, String> params;
    private final Headers headers;
    private final boolean inProgress;
    private final boolean outProgress;
    private OkResponse<?> mockResponse;

    //发起请求 解析相关
    private final OkConfig okConfig;
    //额外入参
    private final Map<String, Object> extraMap;

    protected OkRequest(Builder<?> builder) {
        this.okConfig = builder.okConfig;
        this.url = builder.resolveUrl;
        //如果没有设置 默认为POST
        this.method = builder.method;
        this.tag = ParamUtil.defValueIfNull(builder.tag, this);
        this.params = builder.params;
        this.headers = builder.headers.build();
        this.inProgress = builder.inProgress;
        this.outProgress = builder.outProgress;
        this.mockResponse = builder.mockResponse;
        this.extraMap = builder.extraMap;
    }

    public String method() {
        return method;
    }

    public HttpUrl url() {
        return url;
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

    public OkConfig okConfig() {
        return okConfig;
    }

    public Object tag() {
        return tag;
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

    @Nullable
    public OkResponse<?> mockResponse() {
        return mockResponse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{method="
                + method
                + ",url="
                + url
                + ", tag="
                + (tag != this ? tag : null)
                + '}';
    }

    public abstract Request createRequest();

    /**
     * Created by cx on 17/6/22.
     */
    @SuppressWarnings("unchecked")
    protected static abstract class Builder<T extends Builder> {
        private String method;
        private String url;
        private HttpUrl resolveUrl;

        private Object tag;
        private Headers.Builder headers;

        /**
         * 保存http请求String类型参数，如Get请求查询参数，form表单等，根据需要子类自己实现
         */
        private final Map<String, String> params = new LinkedHashMap<>();

        private boolean inProgress;
        private boolean outProgress;

        private Map<String, Object> extraMap;
        private Map<String, String> paths;

        private OkResponse<?> mockResponse;
        //http请求通用的配置
        private OkConfig okConfig;

        public Builder() {
            //如果没有设置 默认为POST
            this.method = POST;
            this.headers = new Headers.Builder();
            this.inProgress = false;
            this.outProgress = false;
        }

        public T method(@Method String method) {
            this.method = checkNotNull(method, "method==null");
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

        public T okConfig(OkConfig okConfig) {
            this.okConfig = checkNotNull(okConfig, "okConfig==null");
            return (T) this;
        }

        /**
         * 设置Http请求的判断url地址，see {@link HttpUrl#resolve(String)}
         *
         * @param url http请求的url
         */
        public T url(String url) {
            checkNotNull(url, "url==null");
            this.url = url;
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

        public T path(String key, Object value) {
            checkNotNull(key, "key==null");
            checkNotNull(value, "value==null");
            String result = String.valueOf(value);
            if (paths == null) {
                //Lazy Initialization
                paths = new LinkedHashMap<>();
            }
            paths.put(key, result);
            return (T) this;
        }

        /**
         * @param key   param 中的 key
         * @param value 调用 {@link String#valueOf(Object)}转换成 String对象
         * @return Builder
         */
        public T param(String key, Object value) {
            checkNotNull(key, "key==null");
            String result = null;
            if (value != null) {
                result = String.valueOf(value);
            }
            params.put(key, result);
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

        /**
         * @param mockResponse if is null,will call Http
         */
        public T mockResponse(OkResponse<?> mockResponse) {
            this.mockResponse = mockResponse;
            return (T) this;
        }

        @CallSuper
        public OkRequest build() {
            if (okConfig == null) {
                //设置为全局的,这样做的目的可以动态设置地址，和解析器等,如果没有设置此方法会奔溃
                okConfig = EasyOkHttp.okConfig();
            }
            if (okConfig.mustTag()) {
                checkState(tag != null, "tag==null");
            }
            checkState(url != null, "url==null");

            if (ParamUtil.isEmpty(paths)) {
                for (Map.Entry<String, String> entry : paths.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            //构造Url
            HttpUrl baseUrl = okConfig.baseUrl();
            HttpUrl resolveUrl = baseUrl.resolve(url);
            if (resolveUrl == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", url: " + url);
            }
            this.resolveUrl = resolveUrl;
            return null;
        }
    }
}
