package com.xcheng.okhttp.util;

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

    public static boolean isEmpty(Map<?, ?> map) {
        //因为它不能够确信加入到list中的值具有正确的类型。
        //意思是使用了一个未经处理的类型，它不能验证代码是类型安全的。
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 如果checkValue 为null返回defValue
     *
     * @return 不为空的对象
     */
    public static <T> T defValueIfNull(T checkValue, @NonNull T defValue) {
        return checkValue != null ? checkValue : defValue;
    }

    /**
     * Returns an immutable copy of {@code list}.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

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

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
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
