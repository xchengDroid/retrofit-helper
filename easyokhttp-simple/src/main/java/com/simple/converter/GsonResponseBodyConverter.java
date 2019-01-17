package com.simple.converter;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.simple.okhttp.Tip;
import com.xcheng.retrofit.BaseGsonConverter;
import com.xcheng.retrofit.HttpError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：json解析相关
 */
final class GsonResponseBodyConverter<T> extends BaseGsonConverter<T> {
    private final Gson gson;

    GsonResponseBodyConverter(Gson gson, Type type) {
        super(type, $Gson$Types.getRawType(type));
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
            if (Tip.class == rawType) {
                return (T) tip;
            }
            //这样判断能防止服务端忽略data字段导致jsonObject.get("data")方法奔溃
            //且能判断为null或JSONObject#NULL的情况
            if (jsonObject.isNull("data")) {
                throw new HttpError("数据为空", tip);
            }
            Object data = jsonObject.get("data");
            if (isEmptyJSON(data)) {
                throw new HttpError("暂无数据", tip);
            }
            //data 基础类型 如{"msg": "xxx","code": xxx,"data": xxx}
            T t = convertBaseType(data, rawType);
            if (t != null) {
                return t;
            }
            t = gson.fromJson(data.toString(), type);
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
