package com.xcheng.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

/**
 * Created by dingjikerbo on 2016/9/18.
 */
public interface ProxyInterceptor {
    boolean onIntercept(@NonNull Object object, @NonNull Method method, @Nullable Object[] args);
}
