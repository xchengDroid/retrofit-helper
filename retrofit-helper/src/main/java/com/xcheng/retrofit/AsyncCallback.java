package com.xcheng.retrofit;

import androidx.annotation.NonNull;

/**
 * 创建时间：2020/9/9
 * 编写人： chengxin
 * 功能描述：通用简单的异步回调接口类
 */
public interface AsyncCallback<T, E> {
    /**
     * 成功回调
     */
    void onGet(@NonNull T t);

    /**
     * 失败回调
     */
    void onFailure(@NonNull E e);

    /**
     * 线程调度，发送成功到主线程
     */
    void postGet(@NonNull T t);

    /**
     * 线程调度，发送失败到的主线程
     */
    void postFailure(@NonNull E e);
}
