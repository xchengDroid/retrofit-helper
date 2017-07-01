package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;

import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by chengxin on 2017/6/22.
 */
public class StringParse extends ResponseParse<String> {

    @NonNull
    @Override
    public OkResponse<String> parseNetworkResponse(OkCall<String> okCall, Response response, int id) throws IOException {
        if (response.isSuccessful()) {
            return OkResponse.success(response.body().string());
        }
        return OkResponse.error(BaseError.getNotFoundError("not success response"));
    }
}
