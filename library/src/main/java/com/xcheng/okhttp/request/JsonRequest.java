package com.xcheng.okhttp.request;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.util.EasyPreconditions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 提交JSON，默认为POST请求
 * Created by chengxin on 2017/6/22.
 */
public class JsonRequest extends OkRequest {

    private final String json;

    private JsonRequest(Builder builder) {
        super(builder);
        json = builder.json;
    }

    public String json() {
        return json;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(url()).tag(tag()).headers(headers()).method(method(),
                RequestBody.create(MEDIA_TYPE_JSON, json)).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        private String json;
        private Map<String, Object> nameValuePairs;

        public Builder json(String json) {
            this.json = json;
            return this;
        }

        /**
         * 将jsonObject转换成json字符串对象
         */
        public Builder json(@NonNull JSONObject jsonObject) {
            return json(jsonObject.toString());
        }

        /**
         * 将JSONArray转换成json字符串对象
         */
        public Builder json(@NonNull JSONArray jsonArray) {
            return json(jsonArray.toString());
        }

        /**
         * Maps {@code name} to {@code value}, clobbering any existing name/value
         * mapping with the same name.
         *
         * @param value a {@link JSONObject}, {@link JSONArray}, String, Boolean,
         *              Integer, Long, Double,Float,or {@code null}. May not be
         *              {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
         *              infinities}.
         */
        public Builder put(String name, Object value) {
            EasyPreconditions.checkNotNull(name, "name==null");
            if (nameValuePairs == null) {
                nameValuePairs = new LinkedHashMap<>();
            }
            nameValuePairs.put(name, value);
            return this;
        }

        @Override
        public Builder param(String key, String value) {
            throw new UnsupportedOperationException("Unsupported for JsonRequest");
        }

        @Override
        public JsonRequest build() {
            if (json == null) {
                if (nameValuePairs == null || nameValuePairs.isEmpty()) {
                    throw new IllegalStateException("json==null,it must have a request body.");
                }
                json = new JSONObject(nameValuePairs).toString();
            }
            return new JsonRequest(this);
        }
    }
}
