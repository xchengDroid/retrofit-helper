package com.xcheng.proxy;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by chengxin on 2020/05/07.
 */
final class ProxyInvocationHandler implements InvocationHandler, ProxyInterceptor {
    @NonNull
    private final Object subject;
    @Nullable
    private final ProxyInterceptor interceptor;

    private final boolean weakRef;

    private final boolean postUI;

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper(), msg -> {
        ProxyBulk.invoke(msg.obj);
        return true;
    });

    ProxyInvocationHandler(@NonNull Object subject, @Nullable ProxyInterceptor interceptor, boolean weakRef, boolean postUI) {
        this.weakRef = weakRef;
        this.interceptor = interceptor;
        this.postUI = postUI;
        this.subject = weakRef ? new WeakReference<>(subject) : subject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object subject = getObject();
        if (!onIntercept(subject, method, args)) {
            ProxyBulk bulk = new ProxyBulk(subject, method, args);
            if (!postUI) {
                return bulk.invoke();
            }
            UI_HANDLER.obtainMessage(0, bulk).sendToTarget();
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
}
