package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

final class RealLifeCall<T> implements LifeCall<T> {

    private final Call<T> delegate;
    private final Lifecycle.Event event;
    private final boolean checkProvider;

    /**
     * LifeCall是否被释放了
     */
    private volatile boolean disposed;

    /**
     * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    RealLifeCall(Call<T> delegate, Lifecycle.Event event, boolean checkProvider) {
        this.delegate = delegate;
        this.event = event;
        this.checkProvider = checkProvider;
    }

    @Override
    public void enqueue(@Nullable final LifecycleProvider provider, final LifeCallback<T> callback) {
        Utils.checkNotNull(callback, "callback==null");
        //make sure if throw  Already executed and other exceptions
        //remove from outside
        addToProvider(provider);
        //postToMainThread ensure queue
        NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (!disposed) {
                    callback.onStart(RealLifeCall.this);
                }
            }
        });

        delegate.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, final Response<T> response) {
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callResult(response, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, final Throwable t) {
                NetTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callResult(null, t);
                    }
                });
            }

            @UiThread
            private void callResult(@Nullable Response<T> response, @Nullable Throwable t) {
                try {
                    if (disposed) {
                        callback.onDisposed(RealLifeCall.this);
                        return;
                    }
                    if (response != null) {
                        T body = response.body();
                        if (body != null) {
                            T transformer = callback.transform(RealLifeCall.this, body);
                            Utils.checkNotNull(transformer == null, "transformer==null");
                            callback.onSuccess(RealLifeCall.this, transformer);
                        } else {
                            t = new HttpError("response.body()==null", response);
                        }
                    }
                    if (t != null) {
                        HttpError error = callback.parseThrowable(RealLifeCall.this, t);
                        Utils.checkNotNull(error == null, "error==null");
                        callback.onError(RealLifeCall.this, error);
                    }
                    callback.onCompleted(RealLifeCall.this, t);
                } finally {
                    removeFromProvider(provider);
                }
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @NonNull
    @Override
    public T execute(@Nullable LifecycleProvider provider) throws Throwable {
        addToProvider(provider);
        try {
            Response<T> response = delegate.execute();
            if (disposed) {
                throw new DisposedException("already disposed");
            }
            T body = response.body();
            if (body != null) {
                return body;
            }
            throw new HttpError("response.body()==null", response);
        } catch (Throwable t) {
            if (disposed && !(t instanceof DisposedException)) {
                throw new DisposedException("already disposed", t);
            }
            throw t;
        } finally {
            removeFromProvider(provider);
        }
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
        return new RealLifeCall<>(delegate.clone(), event, checkProvider);
    }

    @Override
    public Request request() {
        return delegate.request();
    }

    @Override
    public void onChanged(@Nullable Lifecycle.Event event) {
        //just in case
        if (disposed)
            return;
        if (this.event == event || event == Lifecycle.Event.ON_DESTROY) {
            if (RetrofitFactory.SHOW_LOG) {
                Log.d(LifeCall.TAG, "disposed by " + event + " url-->" + request().url());
            }
            disposed = true;
            cancel();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    private void removeFromProvider(@Nullable final LifecycleProvider provider) {
        if (provider != null) {
            NetTaskExecutor.getInstance().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    provider.removeObserver(RealLifeCall.this);
                }
            });
        }
    }

    private void addToProvider(@Nullable final LifecycleProvider provider) {
        if (provider != null) {
            //ensure on MainThread
            NetTaskExecutor.getInstance().executeOnMainThread(new Runnable() {
                @Override
                public void run() {
                    provider.observe(RealLifeCall.this);
                }
            });
        } else {
            if (checkProvider) {
                throw new NullPointerException("lifecycleProvider==null");
            }
            if (RetrofitFactory.SHOW_LOG) {
                Log.w(LifeCall.TAG, "lifecycleProvider is null, lifecycle will not provide");
            }
        }
    }
}