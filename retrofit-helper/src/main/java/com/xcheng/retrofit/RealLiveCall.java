package com.xcheng.retrofit;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

final class RealLiveCall<T> extends LiveCall<T> {
    static String TAG = RealLiveCall.class.getSimpleName();
    private final Call<T> delegate;
    private final AtomicBoolean started = new AtomicBoolean(false);

    private RealLiveCall(Call<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onActive() {
        super.onActive();
        Log.d(TAG, "onInactive");
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        Log.d(TAG, "onInactive");
    }

    @Override
    public Response<T> execute() throws IOException {
        return null;
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @Override
    public LiveCall<T> clone() {
        return new RealLiveCall<>(delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }
}