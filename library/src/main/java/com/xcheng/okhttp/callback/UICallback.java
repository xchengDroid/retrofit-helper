package com.xcheng.okhttp.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.xcheng.okhttp.error.BaseError;

import okhttp3.Response;

public abstract class UICallback<T> {
    @UiThread
    public void onBefore(int id) {

    }

    @UiThread
    public void onAfter(int id) {

    }

    /**
     * UI Thread
     *
     * @param progress
     */
    @UiThread
    public void inProgress(float progress, long total, int id) {

    }

    @UiThread
    public abstract void onError(@NonNull BaseError error, @Nullable Response noBody, int id);

    @UiThread
    public abstract void onSuccess(@NonNull T response, int id);

}