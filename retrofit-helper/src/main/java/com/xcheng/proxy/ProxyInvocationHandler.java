package com.xcheng.proxy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by chengxin on 2020/05/07.
 */
final class ProxyInvocationHandler implements InvocationHandler, ProxyInterceptor, Handler.Callback {
    @NonNull
    private final Object subject;
    @Nullable
    private final ProxyInterceptor interceptor;
    private final boolean weakRef;
    @Nullable
    private final Handler handler;

    ProxyInvocationHandler(@NonNull Object subject, @Nullable ProxyInterceptor interceptor, boolean weakRef, @Nullable Looper looper) {
        this.weakRef = weakRef;
        this.interceptor = interceptor;
        this.subject = weakRef ? new WeakReference<>(subject) : subject;
        this.handler = looper != null ? new Handler(looper, this) : null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object subject = getObject();
        if (!onIntercept(subject, method, args)) {
            ProxyBulk bulk = new ProxyBulk(subject, method, args);
            if (handler == null) {
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
}
