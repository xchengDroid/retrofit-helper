package com.xcheng.okhttp.request;

import com.xcheng.okhttp.utils.OkExceptions;
import com.xcheng.okhttp.utils.ParamHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 提交字符串
 * Created by chengxin on 2017/6/22.
 */

public class PostStrRequest extends OkRequest {
    private static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private String content;
    private MediaType mediaType;

    protected PostStrRequest(Builder builder) {
        super(builder);
        content = builder.content;
        mediaType = builder.mediaType;
        if (this.content == null) {
            OkExceptions.illegalArgument("the content can not be null !");
        }
        if (mediaType == null) {
            mediaType = MEDIA_TYPE_PLAIN;
        }
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(getUrl()).tag(getTag()).headers(getHeaders().build()).post(RequestBody.create(mediaType, content)).build();
    }

    public static class Builder extends OkRequestBuilder<Builder> {
        private MediaType mediaType;
        private String content;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder mediaType(MediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public OkRequest json(String json) {
            if (mediaType == null) {
                mediaType(MEDIA_TYPE_JSON);
            }
            return content(json).build();
        }

        public OkRequest jsonObject(JSONObject jsonObject) {
            if (jsonObject.length() == 0) {
                OkExceptions.illegalArgument("jsonObject can not be empty");
            }
            return json(jsonObject.toString());
        }

        public OkRequest jsonParams() {
            Map<String, String> params = getParams();
            if (ParamHelper.checkMapEmpty(params)) {
                OkExceptions.illegalArgument("params can not be null or empty");
            }
            JSONObject jsonObject = new JSONObject();
            for (String key : params.keySet()) {
                try {
                    jsonObject.put(key, params.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return json(jsonObject.toString());
        }

        @Override
        public PostStrRequest build() {
            return new PostStrRequest(this);
        }
    }
}
