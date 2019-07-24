package com.xcheng.retrofit;

import android.arch.lifecycle.LiveData;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Response;

public abstract class LiveCall<T> extends LiveData<T> implements Cloneable {

    public abstract Response<T> execute() throws IOException;

    public abstract boolean isExecuted();

    public abstract void cancel();

    public abstract boolean isCanceled();

    public abstract LiveCall<T> clone();

    public abstract Request request();

}