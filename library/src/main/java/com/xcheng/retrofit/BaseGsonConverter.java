package com.xcheng.retrofit;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 创建时间：2018/12/11
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

    /**
     * 是否为空的jsonObject对象 {}
     **/
    protected static boolean isEmptyJSON(Object data) {
        return data instanceof JSONObject && ((JSONObject) data).length() == 0;
    }

    /**
     * 是否为空的{@link JSONObject#NULL}对象
     **/
    protected static boolean isNullJSON(Object data) {
        return data == JSONObject.NULL;
    }

    /**
     * data 如{"msg": "xxx","code": xxx,"data": xxx}
     * 解析基础装箱类型的参数,子类可重载扩展
     * String|Boolean|Integer|Long|Short|Double|Float|Byte
     * <p>
     * if data==null return null
     * <p>
     * 重新定义泛型V ，不限制其必须为T，更灵活。
     */
    @Nullable
    @SuppressWarnings("unchecked")
    protected static <V> V convertBaseType(@Nullable Object data, Class<?> baseType) {
        //如果是String 直接返回
        if (String.class == baseType) {
            return (V) String.valueOf(data);
        }
        if (Boolean.class == baseType && data instanceof Boolean) {
            return (V) data;
        }
        //检测是否为装箱类型
        if (!(data instanceof Number)) {
            return null;
        }
        //防止JSON不是引用我们想要的类型
        Number number = (Number) data;
        //赋值时自动装箱
        Number value = null;
        if (Integer.class == baseType) {
            value = number.intValue();
        } else if (Long.class == baseType) {
            value = number.longValue();
        } else if (Short.class == baseType) {
            value = number.shortValue();
        } else if (Double.class == baseType) {
            value = number.doubleValue();
        } else if (Float.class == baseType) {
            value = number.floatValue();
        } else if (Byte.class == baseType) {
            value = number.byteValue();
        }
        return (V) value;
    }
}
