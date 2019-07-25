package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Response;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：添加重载方法{@link LifeCall#enqueue(Object, LifeCallback)}方法
 */
public interface LifeCall<T> {

    Response<T> execute() throws IOException;

    void enqueue(LifeCallback<T> callback2);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    LifeCall<T> clone();

    Request request();

    LifeCall<T> bindUntilEvent(@NonNull LifecycleProvider provider, @NonNull Lifecycle.Event event);

    LifeCall<T> bindUntilDestroy(@NonNull LifecycleProvider provider);

    void onEvent(@Nullable Lifecycle.Event event);

}
