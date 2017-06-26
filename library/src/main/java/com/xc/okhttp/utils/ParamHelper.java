package com.xc.okhttp.utils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chengxin on 2017/6/22.
 */

public class ParamHelper {
    public static String appendParams(String url, Map<String, String> params) {
        if (url == null || checkMapEmpty(params)) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            builder.appendQueryParameter(key, params.get(key));
        }
        return builder.build().toString();
    }

    public static boolean checkMapEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static Type getType(Class<?> clazz) {
        String clsName = clazz.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java."))
            return TypeToken.get(Object.class).getType();
        Type superType = clazz.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            return getSuperclassTypeParameter(clazz);
        } else {
            return getType(clazz.getSuperclass());
        }
    }

    public static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    public static <C> TypeToken<List<C>> createListTypeToken(@NonNull Class<?> tokenClazz) {
        return (TypeToken<List<C>>) TypeToken.getParameterized(new TypeToken<List>() {
        }.getRawType(), getType(tokenClazz));
    }

    @SuppressWarnings("unchecked")
    public static <C> TypeToken<C> createTypeToken(@NonNull Class<?> tokenClazz) {
        return (TypeToken<C>) TypeToken.get(getType(tokenClazz));
    }
}
