package com.xcheng.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.SkipCallbackExecutor;

public final class CallAdapterFactory extends CallAdapter.Factory {
    private static final String RETURN_TYPE = Call.class.getSimpleName();

    public static final CallAdapter.Factory INSTANCE = new CallAdapterFactory();

    private CallAdapterFactory() {
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
        if (getRawType(returnType) != Call.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    String.format("%s return type must be parameterized as %s<Foo> or %s<? extends Foo>", RETURN_TYPE, RETURN_TYPE, RETURN_TYPE));
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        //支持SkipCallbackExecutor
        final Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class)
                ? null
                : retrofit.callbackExecutor();
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(retrofit2.Call<Object> call) {
                if (executor != null) {
                    return new RealCall<>(executor, call);
                }
                return new RealCall<>(OptionalExecutor.get(), call);
            }
        };
    }
}
