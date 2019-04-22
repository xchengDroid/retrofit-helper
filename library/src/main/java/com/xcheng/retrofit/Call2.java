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

    /**
     * 创建时间：2019/2/27
     * 编写人： chengxin
     * 功能描述：请求被取消保存取消的信息
     */
    final class Cancel {
        //是否为okhttp框架内部调用cancel()方法
        public final boolean fromFrame;
        //请求被取消的时候 可能抛出的异常
        @Nullable
        public final Throwable failure;

        Cancel(boolean fromFrame, @Nullable Throwable failure) {
            this.fromFrame = fromFrame;
            this.failure = failure;
        }
    }
}
