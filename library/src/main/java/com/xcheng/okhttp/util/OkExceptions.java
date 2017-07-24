package com.xcheng.okhttp.util;

import android.support.annotation.Nullable;

public class OkExceptions {
    public static void illegalArgument(String msg, Object... params) {
        throw new IllegalArgumentException(String.format(msg, params));
    }

    public static void illegalState(String msg, Object... params) {
        throw new IllegalStateException(String.format(msg, params));
    }

    public static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}