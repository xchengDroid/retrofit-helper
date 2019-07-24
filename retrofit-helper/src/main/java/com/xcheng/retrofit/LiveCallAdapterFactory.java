package com.xcheng.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * just for android post UI thread
 */
public final class LiveCallAdapterFactory extends CallAdapter.Factory {

    public static final CallAdapter.Factory INSTANCE = new LiveCallAdapterFactory();

    private LiveCallAdapterFactory() {
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
        if (getRawType(returnType) != LiveCall.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "LiveCall return type must be parameterized as LiveCall<Foo> or LiveCall<? extends Foo>");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

//        final Executor callbackExecutor = retrofit.callbackExecutor();
//        if (callbackExecutor == null) throw new AssertionError();

        return new CallAdapter<Object, LiveCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public LiveCall<Object> adapt(Call<Object> call) {
                return new RealLiveCall<>(call);
            }
        };
    }
}
