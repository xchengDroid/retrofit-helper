package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.SkipCallbackExecutor;

public final class HttpQueueAdapterFactory extends CallAdapter.Factory {
    public static final CallAdapter.Factory INSTANCE = new HttpQueueAdapterFactory();

    private HttpQueueAdapterFactory() {
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
        if (getRawType(returnType) != HttpQueue.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("HttpQueue return type must be parameterized " +
                    "as HttpQueue<Foo> or HttpQueue<? extends Foo>");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        //支持SkipCallbackExecutor
        @NonNull final Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class)
                ? OptionalExecutor.getExecutor()
                : OptionalExecutor.getMainThreadExecutor();
        return new CallAdapter<Object, HttpQueue<?>>() {
            @NonNull
            @Override
            public Type responseType() {
                return responseType;
            }

            @NonNull
            @Override
            public HttpQueue<Object> adapt(retrofit2.Call<Object> call) {
                return new RealHttpQueue<>(executor, call);
            }
        };
    }
}
