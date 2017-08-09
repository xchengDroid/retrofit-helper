package com.xcheng.okhttp.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * http请求参数帮助类
 * Created by chengxin on 2017/6/22.
 */

public class ParamUtil {
    public static String appendParams(String url, Map<String, String> params) {
        if (url == null || isEmpty(params)) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Returns an immutable copy of {@code list}.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    @SuppressWarnings("unchecked")
    public static Type getType(Class<?> clazz) {
        String clsName = clazz.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java."))
            return Object.class;
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
        return parameterized.getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    public static <C> TypeToken<List<C>> createListTypeToken(@NonNull Class<?> tokenClazz) {
        return (TypeToken<List<C>>) TypeToken.getParameterized(List.class, getType(tokenClazz));
    }

    @SuppressWarnings("unchecked")
    public static <C> TypeToken<C> createTypeToken(@NonNull Class<?> tokenClazz) {
        return (TypeToken<C>) TypeToken.get(getType(tokenClazz));
    }
}
