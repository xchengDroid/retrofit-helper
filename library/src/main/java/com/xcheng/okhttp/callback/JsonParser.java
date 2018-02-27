package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.OkCall;
import com.xcheng.okhttp.request.OkResponse;

import java.io.IOException;

import okhttp3.Response;

/**
 * JSON解析类
 * Created by chengxin on 2017/6/22.
 */
public class JsonParser<T> extends ErrorParser<T> {
    public static final JsonParser<?> INSTANCE = new JsonParser<>();
    public static final String TYPETOKEN = "typeToken";

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public OkResponse<T> parseNetworkResponse(OkCall<T> call, Response response) throws IOException {
        if (response.isSuccessful()) {
            //可在request中设置TypeToken, extra("typeToken",new TypeToken<T>(){})
            TypeToken<T> token = call.request().extra(TYPETOKEN);
            //如果extra里面没有，尝试获取okCall里面的Type
            if (token == null) {
                token = (TypeToken<T>) TypeToken.get(call.getType());
            }
            String str = response.body().string();
            if (String.class.isAssignableFrom(token.getRawType())) {
                return OkResponse.success((T) str);
            }
            T body = new Gson().fromJson(str, token.getType());
            return OkResponse.success(body);
        }
        return OkResponse.error(new EasyError("response error"));
    }
}
