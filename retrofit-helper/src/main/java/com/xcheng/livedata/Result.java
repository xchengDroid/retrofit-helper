package com.xcheng.livedata;


import android.text.TextUtils;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 创建时间：2020-05-19
 * 编写人： chengxin
 * 功能描述：通用的结果包装类,避免通过抛出异常的方式来处理异常信息
 * like retrofit rxjava Result class,{@link retrofit2.Response}
 */
public final class Result<T> {

    /**
     * @param body 请求成功返回的body
     * @throws NullPointerException if body==null
     */
    @CheckResult
    public static <T> Result<T> body(@NonNull T body) {
        return body(body, "操作成功");
    }

    /**
     * @param body 请求成功返回的body
     * @throws NullPointerException if body==null
     */
    @CheckResult
    public static <T> Result<T> body(@NonNull T body, @NonNull String msg) {
        //noinspection ConstantConditions
        if (body == null) { //二次检测防止为空
            throw new NullPointerException("body==null");
        }
        return new Result<>(body, msg);
    }

    @CheckResult
    public static <T> Result<T> msg(@NonNull String msg) {
        return new Result<>(null, msg);
    }

    /**
     * 成功或失败的描述信息
     */
    @NonNull
    public final String msg;
    @Nullable
    public final T body;

    private Result(@Nullable T body, @NonNull String msg) {
        this.body = body;
        //just in case
        this.msg = TextUtils.isEmpty(msg) ? "" : msg;
    }

    /**
     * 如果body==null,则返回defValue
     *
     * @param defValue 针对Unboxing 包装类为空的问题
     */
    @NonNull
    public T getBody(@NonNull T defValue) {
        return body != null ? body : defValue;
    }
}
