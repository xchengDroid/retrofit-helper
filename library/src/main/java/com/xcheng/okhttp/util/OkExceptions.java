package com.xcheng.okhttp.util;

import android.support.annotation.Nullable;

public class OkExceptions {
    /**
     * 如果condition为true 抛出 IllegalStateException
     *
     * @param condition
     * @param message   异常错误信息
     */
    public static void checkState(boolean condition, String message) {
        if (condition) {
            throw new IllegalStateException(message);
        }
    }

    public static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    /**
     * 如果 condition 为true 抛出异常 IllegalArgumentException
     *
     * @param condition
     * @param message   异常错误信息
     */
    public static void checkArgument(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}