package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 创建时间：2019-09-04
 * 编写人： chengxin
 * 功能描述：代理{@link okhttp3.Call.Factory} 拦截{@link #newCall(Request)}方法
 */
public abstract class CallFactoryProxy implements Call.Factory {
    protected final Call.Factory delegate;

    public CallFactoryProxy(@NonNull Call.Factory delegate) {
        Utils.checkNotNull(delegate, "delegate==null");
        this.delegate = delegate;
    }

    @Override
    public Call newCall(Request request) {
        Request newRequest = newRequest(request);
        if (newRequest != null) {
            return delegate.newCall(request);
        }
        return delegate.newCall(request);
    }

    /**
     * @param request old request
     * @return new request, if null use old request.
     */
    @Nullable
    protected abstract Request newRequest(Request request);
}
