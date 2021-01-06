package com.xcheng.retrofit;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.Response;

public class TestRealCall<T> implements retrofit2.Call<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    public TestRealCall(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public Response<T> execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(retrofit2.Callback<T> callback) {

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

    @SuppressWarnings("MethodDoesntCallSuperMethod") // Performing deep clone.
    @Override
    public retrofit2.Call<T> clone() {
        return new TestRealCall<>(callbackExecutor, delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }

}