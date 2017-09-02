package com.xcheng.okhttp.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class EasyPreconditions {

    private EasyPreconditions() {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果condition为false 抛出 IllegalStateException
     *
     * @param condition 抛出异常的条件
     * @param message   异常错误信息
     */
    public static void checkState(boolean condition, String message) {
        if (!condition) {
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
     * 如果 condition 为false 抛出异常 IllegalArgumentException
     *
     * @param condition 抛出异常的条件
     * @param message   异常错误信息
     */
    public static void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures that a CharSequence passed as a parameter to the calling method is
     * not null and not empty.
     *
     * @param reference an CharSequence reference
     * @return the non-empty reference that was validated
     * @throws IllegalArgumentException if {@code reference} was null or empty
     */
    public static <T extends CharSequence> T checkNotEmpty(final T reference, String message) {
        if (TextUtils.isEmpty(reference)) {
            throw new IllegalArgumentException(message);
        }
        return reference;
    }
}