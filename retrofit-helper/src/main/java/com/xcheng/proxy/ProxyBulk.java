package com.xcheng.proxy;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

/**
 * Created by chengxin on 2020/05/07.
 */
public class ProxyBulk {
    public final Object object;
    public final Method method;
    public final Object[] args;

    public ProxyBulk(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this.args = args;
    }

    public Object invoke() {
        try {
            return method.invoke(object, args);
        } catch (Throwable e) {
            //转化成运行时异常
            throw new IllegalStateException(e);
        }
    }

    public static Object invoke(@NonNull Object obj) {
        return ((ProxyBulk) obj).invoke();
    }
}
