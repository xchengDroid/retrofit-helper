package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class RealLifeCall<T> implements LifeCall<T> {

    private final Call<T> delegate;
    private final Lifecycle.Event event;
    @Nullable
    private final LifecycleProvider provider;

    //是否回收了
    private boolean onLifecycle;

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    RealLifeCall(Call<T> delegate, Lifecycle.Event event) {
        this.delegate = delegate;
        this.event = event;
        this.provider = delegate.request().tag(LifecycleProvider.class);
        if (provider == null) {
            if (RetrofitFactory.ERROR_WHEN_NO_PROVIDER) {
                throw new IllegalStateException("Missing (@Tag LifecycleProvider provider) parameter in method");
            } else {
                Log.w(LifeCall.TAG, "Can not find LifecycleProvider in request.tag(), lifecycle will not provide");
            }
        }
    }

    @Override
    public void enqueue(final LifeCallback<T> lifeCallback) {
        addToProvider();
        NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
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
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callResult(lifeCallback, response, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, final Throwable t) {
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
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
        removeFromProvider();
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
        addToProvider();
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
        return new RealLifeCall<>(delegate.clone(), event);
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public void onChanged(@Nullable Lifecycle.Event event) {
        if (!onLifecycle && this.event == event) {
            onLifecycle = true;
            cancel();
        }
    }


    /**
     * ensure observe on MainThread
     */
    private void addToProvider() {
        if (provider != null) {
            NetTaskExecutor.getInstance().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    provider.observe(RealLifeCall.this);
                }
            });
        }
    }

    private void removeFromProvider() {
        if (provider != null) {
            NetTaskExecutor.getInstance().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    provider.removeObserver(RealLifeCall.this);
                }
            });
        }
    }
}