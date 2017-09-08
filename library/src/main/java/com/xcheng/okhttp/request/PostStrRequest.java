package com.xcheng.okhttp.request;

import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.ParamUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 提交字text,json等
 * Created by chengxin on 2017/6/22.
 */

public class PostStrRequest extends OkRequest {
    private static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private String content;
    private MediaType mediaType;

    private PostStrRequest(Builder builder) {
        super(builder);
        content = builder.content;
        mediaType = ParamUtil.defValueIfNull(builder.mediaType, MEDIA_TYPE_PLAIN);
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(url()).tag(tag()).headers(headers()).post(RequestBody.create(mediaType, content)).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
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
            //如果mediaType==null 默认设置为MEDIA_TYPE_JSON
            if (mediaType == null) {
                mediaType(MEDIA_TYPE_JSON);
            }
            return content(json).build();
        }

        public OkRequest jsonObject(JSONObject jsonObject) {
            EasyPreconditions.checkArgument(jsonObject.length() != 0, "jsonObject.length() == 0");
            return json(jsonObject.toString());
        }

        public OkRequest jsonParams() {
            Map<String, String> params = getParams();
            EasyPreconditions.checkArgument(!ParamUtil.isEmpty(params), "params can not be null or empty");
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
            EasyPreconditions.checkState(content != null, "content==null,it must have a request body.");
            return new PostStrRequest(this);
        }
    }
}
