package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class RealLifeCall<T> implements LifeCall<T> {

    private final Executor callbackExecutor;
    private final Call<T> delegate;
    private final Lifecycle.Event lifeEvent;
    //是否回收了
    private boolean onLifecycle;

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    RealLifeCall(Executor callbackExecutor, Call<T> delegate, Lifecycle.Event event) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
        this.lifeEvent = event;
    }

    @Override
    public void enqueue(final LifeCallback<T> lifeCallback) {
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!onLifecycle) {
                    lifeCallback.onStart(RealLifeCall.this);
                }
            }
        });

        delegate.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, final Response<T> response) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callResult(lifeCallback, response, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callResult(lifeCallback, null, t);
                    }
                });
            }
        });
    }

    @UiThread
    private void callResult(LifeCallback<T> lifeCallback, @Nullable Response<T> response, @Nullable Throwable failureThrowable) {
        if (onLifecycle)
            return;
        //1、获取解析结果
        Result<T> result;
        if (response != null) {
            result = lifeCallback.parseResponse(this, response);
            Utils.checkNotNull(result, "result==null");
        } else {
            Utils.checkNotNull(failureThrowable, "failureThrowable==null");
            HttpError error = lifeCallback.parseThrowable(this, failureThrowable);
            result = Result.error(error);
        }
        //2、回调成功失败
        if (result.isSuccess()) {
            lifeCallback.onSuccess(this, result.body());
        } else {
            lifeCallback.onError(this, result.error());
        }
        lifeCallback.onCompleted(this, failureThrowable, isCanceled());
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public Response<T> execute() throws IOException {
        return delegate.execute();
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
    public LifeCall<T> clone() {
        return new RealLifeCall<>(callbackExecutor, delegate.clone(), lifeEvent);
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public LifeCall<T> bindToLifecycle(@NonNull LifecycleProvider provider) {
        provider.bindToLifecycle(this);
        return this;
    }

    @Override
    public void onEvent(@NonNull Lifecycle.Event event) {
        if (!onLifecycle && lifeEvent == event) {
            onLifecycle = true;
            cancel();
        }
    }
}