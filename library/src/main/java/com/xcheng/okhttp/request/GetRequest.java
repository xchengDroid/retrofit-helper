package com.xcheng.okhttp.request;

import com.xcheng.okhttp.utils.OkExceptions;
import com.xcheng.okhttp.utils.Util;

import okhttp3.Request;

/**
 * Created by chengxin on 2017/6/22.
 */

public class GetRequest extends OkRequest {

    protected GetRequest(Builder builder) {
        super(builder);
    }

    @Override
    protected Request createRequest() {
        return new Request.Builder().url(url()).headers(headers()).tag(tag()).build();
    }

    public static class Builder extends OkRequest.Builder<Builder> {
        @Override
        public GetRequest build() {
            String url = getUrl();
            OkExceptions.checkNotNull(url, "url==null");
            url = Util.appendParams(url, getParams());
            url(url);
            return new GetRequest(this);
        }
    }
}
