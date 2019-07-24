package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import retrofit2.Response;

@UiThread
public interface LifeCallback<T> {

    void onStart(LifeCall<T> call);

    @NonNull
    Result<T> parseResponse(LifeCall<T> call, Response<T> response);

    @NonNull
    HttpError parseThrowable(LifeCall<T> call, Throwable t);

    void onError(LifeCall<T> call, HttpError error);

    void onSuccess(LifeCall<T> call, T response);

    void onCompleted(LifeCall<T> call, @Nullable Throwable t);
}