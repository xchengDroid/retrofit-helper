package com.xcheng.proxy;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 构建动态代理对象
 */
public final class ProxyBuilder {
    @NonNull
    private Object subject;
    @Nullable
    private ProxyInterceptor interceptor;
    private boolean weakRef;
    private boolean postUI;
    private Handler handler;

    private ProxyBuilder(@NonNull Object subject) {
        this.subject = subject;
        this.weakRef = false;
        this.postUI = false;
    }

    public ProxyBuilder weakRef(boolean weakRef) {
        this.weakRef = weakRef;
        return this;
    }

    public ProxyBuilder postUi() {
        this.postUI = true;
        return this;
    }

    public ProxyBuilder interceptor(@Nullable ProxyInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public static ProxyBuilder newBuilder(@NonNull Object subject) {
        return new ProxyBuilder(subject);
    }
}