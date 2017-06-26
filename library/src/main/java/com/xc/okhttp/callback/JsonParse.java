package com.xc.okhttp.callback;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.xc.okhttp.error.BaseError;
import com.xc.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by chengxin on 2017/6/22.
 */

public class JsonParse<T> extends ResponseParse<T> {
    @NonNull
    @Override
    public OkResponse<T> parseNetworkResponse(OkCall<T> call, Response response, int id) throws IOException {
        if (response.isSuccessful()) {
            T body = new Gson().fromJson(response.body().string(), call.getTypeToken().getType());
            return OkResponse.success(body);
        }
        return OkResponse.error(BaseError.getNotFoundError("not success response"));
    }
}
