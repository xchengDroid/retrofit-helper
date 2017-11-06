package com.xcheng.okhttp.request;

import com.xcheng.okhttp.util.ParamUtil;

import okhttp3.Request;

/**
 * 通用的GET请求
 * Created by chengxin on 2017/6/22.
 */
public class GetRequest extends OkRequest {
    private final String appendUrl;

    private GetRequest(Builder builder) {
        super(builder);
        appendUrl = ParamUtil.appendParams(url(), params());
    }

    public String getAppendUrl() {
        return appendUrl;
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(appendUrl).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        @Override
        public GetRequest build() {
            return new GetRequest(this);
        }
    }
}
