package com.xc.okhttp.callback;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.xc.okhttp.error.BaseError;

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
    public abstract void onError(@NonNull BaseError error, int id);

    @UiThread
    public abstract void onSuccess(@NonNull T response, int id);

}