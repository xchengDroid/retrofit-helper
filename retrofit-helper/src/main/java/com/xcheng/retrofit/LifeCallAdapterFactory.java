package com.xcheng.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.CallAdapter;
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
                    "Call return type must be parameterized as Call2<Foo> or Call2<? extends Foo>");
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
                return new LifeCallbackCall<>(callbackExecutor, call);
            }
        };
    }
}
