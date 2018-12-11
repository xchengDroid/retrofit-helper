package com.xcheng.retrofit;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：基础json解析相关
 */
public abstract class BaseGsonConverter<T> implements Converter<ResponseBody, T> {
    protected final Type type;
    protected final Class<?> rawType;

    protected BaseGsonConverter(Type type, Class<?> rawType) {
        this.type = type;
        this.rawType = rawType;
    }

    protected BaseGsonConverter(Type type) {
        this(type, ExecutorCallAdapterFactory.getRawType(type));
    }

    /**
     * 是否为空的jsonObject对象
     **/
    protected static boolean isEmptyJSON(Object data) {
        return data instanceof JSONObject && ((JSONObject) data).length() == 0;
    }

    /**
     * data 如{"msg": "xxx","code": xxx,"data": xxx}
     * 解析基础装箱类型的参数,子类可重载扩展
     * String|Boolean|Integer|Long|Short|Double|Float
     * <p>
     * if data==null return null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected T convertBaseType(@Nullable Object data) {
        //如果是String 直接返回
        if (data != null && String.class == rawType) {
            return (T) String.valueOf(data);
        }
        if (Boolean.class == rawType && data instanceof Boolean) {
            return (T) data;
        }
        if (!(data instanceof Number)) {
            return null;
        }
        //防止JSON不是引用我们想要的类型
        Number number = (Number) data;
        if (Integer.class == rawType) {
            return (T) Integer.valueOf(number.intValue());
        }
        if (Long.class == rawType) {
            return (T) Long.valueOf(number.longValue());
        }
        if (Short.class == rawType) {
            return (T) Short.valueOf(number.shortValue());
        }
        if (Double.class == rawType) {
            return (T) Double.valueOf(number.doubleValue());
        }
        if (Float.class == rawType) {
            return (T) Float.valueOf(number.floatValue());
        }
        if (Byte.class == rawType) {
            return (T) Byte.valueOf(number.byteValue());
        }
        return null;
    }
}
