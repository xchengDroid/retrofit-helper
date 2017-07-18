package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.error.BaseError;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by chengxin on 2017/6/22.
 */

public class JsonParse<T> extends SimpleParse<T> {
    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public OkResponse<T> parseNetworkResponse(OkCall<T> call, Response response) throws IOException {
        if (response.isSuccessful()) {
            TypeToken<T> token = call.getTypeToken();
            String str = response.body().string();
            if (String.class.isAssignableFrom(token.getRawType())) {
                return OkResponse.success((T) str);
            }
            T body = new Gson().fromJson(str, token.getType());
            return OkResponse.success(body);
        }
        return OkResponse.error(BaseError.createDefaultError("response error"));
    }
}