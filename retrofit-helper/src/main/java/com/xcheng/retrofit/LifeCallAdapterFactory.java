package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * just for android post UI thread
 */
public final class LifeCallAdapterFactory extends CallAdapter.Factory {

    public static final CallAdapter.Factory INSTANCE = new LifeCallAdapterFactory();

    private LifeCallAdapterFactory() {
    }

    /**
     * Extract the raw class type from {@code type}. For example, the type representing
     * {@code List<? extends Runnable>} returns {@code List.class}.
     */
    public static Class<?> getRawType(Type type) {
        return CallAdapter.Factory.getRawType(type);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != LifeCall.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "LifeCall return type must be parameterized as LifeCall<Foo> or LifeCall<? extends Foo>");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

        final Executor callbackExecutor = retrofit.callbackExecutor();
        if (callbackExecutor == null) throw new AssertionError();

        return new CallAdapter<Object, LifeCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public LifeCall<Object> adapt(Call<Object> call) {
                return new LifeCallbackCall2<>(callbackExecutor, call);
            }
        };
    }

    static final class LifeCallbackCall2<T> implements LifeCall<T> {
        private final Executor callbackExecutor;
        private final Call<T> delegate;

        /**
         * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
         * in which case callbacks should be made synchronously on the background thread.
         */
        LifeCallbackCall2(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(final LifeCallback<T> callback) {
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onStart(LifeCallbackCall2.this);
                }
            });

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callResult(callback, response, null);
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callResult(callback, null, t);
                        }
                    });
                }
            });
        }

        @UiThread
        private void callResult(LifeCallback<T> callback, @Nullable Response<T> response, @Nullable Throwable failureThrowable) {
            //1、获取解析结果
            Result<T> result;
            if (response != null) {
                result = callback.parseResponse(this, response);
                Utils.checkNotNull(result, "result==null");
            } else {
                Utils.checkNotNull(failureThrowable, "failureThrowable==null");
                HttpError error = callback.parseThrowable(this, failureThrowable);
                result = Result.error(error);
            }
            //2、回调成功失败
            if (result.isSuccess()) {
                callback.onSuccess(this, result.body());
            } else {
                callback.onError(this, result.error());
            }
            callback.onCompleted(this, failureThrowable);
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
        public LifeCall<T> bindUntilEvent(Lifecycle.Event event) {
            return null;
        }

        @Override
        public LifeCall<T> bindUntilDestroy() {
            return null;
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
            return new LifeCallbackCall2<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }
}
