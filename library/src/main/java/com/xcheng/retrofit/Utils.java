package com.xcheng.retrofit;

import android.support.annotation.Nullable;

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
}
