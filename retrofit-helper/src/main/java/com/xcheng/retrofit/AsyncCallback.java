package com.xcheng.retrofit;

import androidx.annotation.NonNull;

/**
 * 创建时间：2020/9/9
 * 编写人： chengxin
 * 功能描述：通用异步回调接口类
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
}
