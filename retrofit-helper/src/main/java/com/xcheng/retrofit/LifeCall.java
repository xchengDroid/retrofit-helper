package com.xcheng.retrofit;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 */
@SuppressWarnings("JavadocReference")
public interface LifeCall<T> extends Callable<T>, LifecycleProvider.Observer {
    /**
     * Returns true if this call has been disposed.
     *
     * @return true if this call has been disposed
     */
    boolean isDisposed();

    /**
     * @throws Throwable if it is {@link DisposedException},mean that {@code LifeCall} has been disposed
     */
    @Override
    T execute() throws Throwable;
}
