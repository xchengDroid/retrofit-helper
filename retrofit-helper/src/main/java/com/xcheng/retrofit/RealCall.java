package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealCall<T> implements Call<T> {
    private final Executor callbackExecutor;
    private final retrofit2.Call<T> delegate;

    RealCall(Executor callbackExecutor, retrofit2.Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @NonNull
    @Override
    public T execute() throws Throwable {
        try {
            Response<T> response = delegate.execute();
            T body = response.body();
            if (body != null) {
                return body;
            }
            throw new HttpException(response);
        } catch (Throwable t) {
            RetrofitFactory.getOnEventListener().onThrowable(RealCall.this, t);
            throw t;
        }
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                callback.onStart(RealCall.this);
            }
        });
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(retrofit2.Call<T> call, final Response<T> response) {
                final Result<T> result;
                if (response.body() != null) {
                    result = Result.success(response.body());
                } else {
                    result = Result.error(new HttpException(response));
                }
                callResult(result);
            }

            @Override
            public void onFailure(retrofit2.Call<T> call, final Throwable t) {
                callResult(Result.<T>error(t));
            }

            @SuppressWarnings("ConstantConditions")
            @UiThread
            private void callResult(final Result<T> result) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (result.isSuccess()) {
                            T transformer = callback.transform(RealCall.this, result.body());
                            Utils.checkNotNull(transformer == null, "transformer==null");
                            callback.onSuccess(RealCall.this, transformer);
                        } else {
                            HttpError error = callback.parseThrowable(RealCall.this, result.error());
                            Utils.checkNotNull(error == null, "httpError==null");
                            callback.onError(RealCall.this, error);
                            RetrofitFactory.getOnEventListener().onThrowable(RealCall.this, result.error());
                        }
                    }
                });
            }
        });
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

    @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
    @Override
    public Call<T> clone() {
        return new RealCall<>(callbackExecutor, delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public LifeCall<T> bindToLifecycle(LifecycleProvider provider, Lifecycle.Event event) {
        Utils.checkNotNull(provider, "provider==null");
        Utils.checkNotNull(event, "event==null");
        if (event == Lifecycle.Event.ON_ANY) {
            throw new IllegalArgumentException("ON_ANY event is not allowed.");
        }
        return new RealLifeCall<>(clone(), event, provider);
    }

    @Override
    public LifeCall<T> bindUntilDestroy(LifecycleProvider provider) {
        return bindToLifecycle(provider, Lifecycle.Event.ON_DESTROY);
    }
}