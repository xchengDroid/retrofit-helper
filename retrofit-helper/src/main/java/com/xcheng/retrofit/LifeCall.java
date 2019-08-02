package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Response;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：支持生命周期绑定的Call{@link retrofit2.Call}
 */
public interface LifeCall<T> extends Observer<Lifecycle.Event> {

    String TAG = "LifeCall";

    Response<T> execute(@Nullable LifecycleProvider provider) throws IOException;

    void enqueue(@Nullable LifecycleProvider provider, LifeCallback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    LifeCall<T> clone();

    Request request();

    boolean isLifecycle();

}
