package com.xcheng.retrofit;

/**
 * 创建时间：2019-08-08
 * 编写人： chengxin
 * 功能描述： 可同步和异步调用
 */
@SuppressWarnings("JavadocReference")
public interface Callable<T> {
    /**
     * @return result
     * @throws Throwable any exceptions
     */
    T execute() throws Throwable;

    void enqueue(Callback<T> callback);
}
