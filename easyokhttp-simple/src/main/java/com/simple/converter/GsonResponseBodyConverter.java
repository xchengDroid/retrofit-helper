package com.simple.converter;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.simple.okhttp.Tip;
import com.xcheng.retrofit.HttpError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：json解析相关
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    GsonResponseBodyConverter(Gson gson, Type type) {
        this.type = type;
        this.gson = gson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(@NonNull ResponseBody value) throws IOException {
        String cacheStr = value.string();
        try {
            JSONObject jsonObject = new JSONObject(cacheStr);
            final int code = jsonObject.getInt("errorCode");
            final String msg = jsonObject.getString("errorMsg");
            Tip tip = new Tip(code, msg);
            if (code != 0) {
                throw new HttpError(msg, tip);
            }
            Class<?> rawType = $Gson$Types.getRawType(type);
            if (Tip.class == rawType) {
                return (T) tip;
            }
            Object data = jsonObject.get("data");
            if (data == JSONObject.NULL) {
                //in case
                throw new HttpError("暂无数据", tip);
            }
            //如果是String 直接返回
            if (String.class == rawType) {
                return (T) data.toString();
            }
            //data 为Boolean 如{"msg": "手机号格式错误","code": 0,"data": false}
            if (Boolean.class == rawType && data instanceof Boolean) {
                return (T) data;
            }
            //data 为Integer  如{"msg": "手机号格式错误","code": 0,"data": 12}
            if (Integer.class == rawType && data instanceof Integer) {
                return (T) data;
            }
            T t = gson.fromJson(data.toString(), type);
            if (t != null) {
                //防止线上接口修改导致反序列化失败奔溃
                return t;
            }
            throw new HttpError("数据异常", tip);
        } catch (JSONException e) {
            throw new HttpError("解析异常", cacheStr);
        }
    }
}
