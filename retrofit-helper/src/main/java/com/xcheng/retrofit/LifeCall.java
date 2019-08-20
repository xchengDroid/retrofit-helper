package com.xcheng.retrofit;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：实现生命周期绑定的Call{@link retrofit2.Call}
 */
public interface LifeCall<T> extends Callable<T>, LifecycleProvider.Observer {

    /**
     * Returns true if this resource has been disposed.
     *
     * @return true if this resource has been disposed
     */
    boolean isDisposed();

}
