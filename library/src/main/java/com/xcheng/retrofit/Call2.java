package com.xcheng.retrofit;

import android.support.annotation.Nullable;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：添加重载方法{@link Call2#enqueue(Object, Callback2)}方法
 */
public interface Call2<T> extends retrofit2.Call<T> {
    /**
     * @param tag       请求的tag,用于取消请求使用
     * @param callback2 请求的回调
     */
    void enqueue(@Nullable Object tag, Callback2<T> callback2);

    @Override
    Call2<T> clone();
}
