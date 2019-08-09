package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import okhttp3.Request;
import retrofit2.HttpException;
import retrofit2.Response;

final class RealCall<T> implements Call<T> {
    private final retrofit2.Call<T> delegate;

    /**
     * The executor used for {@link retrofit2.Callback} methods on a {@link retrofit2.Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    RealCall(retrofit2.Call<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T execute() throws Throwable {
        Response<T> response = delegate.execute();
        T body = response.body();
        if (body != null) {
            return body;
        }
        throw new HttpException(response);
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        //postToMainThread ensure queue
        NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                callback.onStart(RealCall.this);
            }
        });
        delegate.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(retrofit2.Call<T> call, final Response<T> response) {
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callResult(response, null);
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<T> call, final Throwable t) {
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callResult(null, t);
                    }
                });
            }

            @SuppressWarnings("ConstantConditions")
            @UiThread
            private void callResult(@Nullable Response<T> response, @Nullable Throwable t) {
                if (response != null) {
                    T body = response.body();
                    if (body != null) {
                        T transformer = callback.transform(RealCall.this, body);
                        Utils.checkNotNull(transformer == null, "transformer==null");
                        callback.onSuccess(RealCall.this, transformer);
                    } else {
                        t = new HttpException(response);
                    }
                }
                if (t != null) {
                    HttpError error = callback.parseThrowable(RealCall.this, t);
                    Utils.checkNotNull(error == null, "error==null");
                    callback.onError(RealCall.this, error);
                }
                callback.onCompleted(RealCall.this, t);
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
        return new RealCall<>(delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public LifeCall<T> bindToLifecycle(LifecycleProvider provider, Lifecycle.Event event) {
        Utils.checkNotNull(provider, "provider==null");
        Utils.checkNotNull(event, "event==null");
        return new RealLifeCall<>(clone(), event, provider);
    }

    @Override
    public LifeCall<T> bindToLifecycle(LifecycleProvider provider) {
        return bindToLifecycle(provider, Lifecycle.Event.ON_DESTROY);
    }
}