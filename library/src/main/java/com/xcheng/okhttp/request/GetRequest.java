package com.xcheng.okhttp.request;

import com.xcheng.okhttp.utils.OkExceptions;
import com.xcheng.okhttp.utils.ParamHelper;

import okhttp3.Request;

/**
 * Created by chengxin on 2017/6/22.
 */

public class GetRequest extends OkRequest {

    protected GetRequest(Builder builder) {
        super(builder);
    }

    @Override
    public Request createRequest() {
        return new Request.Builder().url(getUrl()).headers(getHeaders().build()).tag(getTag()).build();
    }

    public static class Builder extends OkRequestBuilder<Builder> {
        @Override
        public GetRequest build() {
            String url = getUrl();
            OkExceptions.checkNotNull(url, "url==null");
            url = ParamHelper.appendParams(url, getParams());
            url(url);
            return new GetRequest(this);
        }
    }
}
