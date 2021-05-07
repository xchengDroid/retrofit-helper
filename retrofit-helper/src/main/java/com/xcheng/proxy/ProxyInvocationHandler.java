package com.xcheng.proxy;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by chengxin on 2016/9/18.
 */
public final class ProxyInvocationHandler implements InvocationHandler, ProxyInterceptor, Callback {

    private final Object subject;

    private final ProxyInterceptor interceptor;

    private final boolean weakRef;

    private final boolean postUI;

    private final Handler handler;

    public ProxyInvocationHandler(Object subject, ProxyInterceptor interceptor, boolean weakRef, boolean postUI) {
        this.weakRef = weakRef;
        this.interceptor = interceptor;
        this.postUI = postUI;
        this.subject = weakRef ? new WeakReference<>(subject) : subject;
        this.handler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object subject = getObject();
        if (!onIntercept(subject, method, args)) {
            ProxyBulk bulk = new ProxyBulk(subject, method, args);
            if (!postUI) {
                return bulk.invoke();
            }
            handler.obtainMessage(0, bulk).sendToTarget();
            return null;
        }
        return null;
    }

    @Override
    public boolean onIntercept(@NonNull Object object, @NonNull Method method, Object[] args) {
        if (interceptor != null) {
            return interceptor.onIntercept(object, method, args);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Object getObject() {
        if (weakRef) {
            return ((WeakReference<Object>) subject).get();
        } else {
            return subject;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        ProxyBulk.invoke(msg.obj);
        return true;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        public Builder() {
        }
    }
}
