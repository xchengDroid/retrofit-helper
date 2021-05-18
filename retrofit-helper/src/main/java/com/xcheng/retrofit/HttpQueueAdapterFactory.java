package com.xcheng.retrofit;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.Call;
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
    public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        if (getRawType(returnType) != HttpQueue.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("HttpQueue return type must be parameterized " +
                    "as HttpQueue<Foo> or HttpQueue<? extends Foo>");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        //支持SkipCallbackExecutor
        final Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class)
                ? null
                : retrofit.callbackExecutor();
        return new CallAdapter<Object, HttpQueue<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public HttpQueue<?> adapt(Call<Object> call) {
                return new RealHttpQueue<>(executor != null ? executor : OptionalExecutor.get(), call);
            }
        };
    }
}
