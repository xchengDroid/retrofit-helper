package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：retrofit2工具类
 */
public class Utils {
    static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

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
}
