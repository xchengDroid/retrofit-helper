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

final class LifeCallbackCall<T> implements LifeCall<T> {

    private  static int a=1;

    private final Executor callbackExecutor;
    private final Call<T> delegate;
    @Nullable
    private volatile Lifecycle.Event lifeEvent;
    //是否回收了
    private volatile boolean onLifecycle;

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    LifeCallbackCall(Executor callbackExecutor, Call<T> delegate) {
        this.callbackExecutor = callbackExecutor;
        this.delegate = delegate;
    }

    @Override
    public void enqueue(final LifeCallback<T> callback2) {
        callbackExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!onLifecycle) {
                    callback2.onStart(LifeCallbackCall.this);
                }
            }
        });

        delegate.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, final Response<T> response) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callResult(callback2, response, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, final Throwable t) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callResult(callback2, null, t);
                    }
                });
            }
        });
    }

    @UiThread
    private void callResult(LifeCallback<T> callback2, @Nullable Response<T> response, @Nullable Throwable failureThrowable) {
        if (onLifecycle)
            return;
        //1、获取解析结果
        Result<T> result;
        if (response != null) {
            result = callback2.parseResponse(this, response);
            Utils.checkNotNull(result, "result==null");
        } else {
            Utils.checkNotNull(failureThrowable, "failureThrowable==null");
            HttpError error = callback2.parseThrowable(this, failureThrowable);
            result = Result.error(error);
        }
        //2、回调成功失败
        if (result.isSuccess()) {
            callback2.onSuccess(this, result.body());
        } else {
            callback2.onError(this, result.error());
        }
        callback2.onCompleted(this, failureThrowable, isCanceled());
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
        return new LifeCallbackCall<>(callbackExecutor, delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public LifeCall<T> bindUntilEvent(@NonNull LifecycleProvider provider, @NonNull Lifecycle.Event event) {
        lifeEvent = event;
        provider.bindLifecycle(this);
        return this;
    }

    @Override
    public LifeCall<T> bindUntilDestroy(@NonNull LifecycleProvider provider) {
        return bindUntilEvent(provider, Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public void onEvent(@Nullable Lifecycle.Event event) {
        if (!onLifecycle && lifeEvent == event) {
            onLifecycle = true;
            cancel();
        }
    }
}