package com.xcheng.okhttp.request;

import com.xcheng.okhttp.util.ParamUtil;

import okhttp3.Request;

/**
 * 通用的GET请求
 * Created by chengxin on 2017/6/22.
 */
public class GetRequest extends OkRequest {

    private GetRequest(Builder builder) {
        super(builder);
    }

    @Override
    public String url() {
        return ParamUtil.appendParams(super.url(), params());
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(url()).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        @Override
        public GetRequest build() {
            return new GetRequest(this);
        }
    }
}
