package com.xcheng.retrofit;

import android.support.annotation.Nullable;

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
    private static final String RETURN_TYPE = LifeCall.class.getSimpleName();

    @Nullable
    private final Executor callbackExecutor;

    private LifeCallAdapterFactory(@Nullable Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * 如果用此构造函数，默认的executor为{@link Retrofit#callbackExecutor()}
     */
    public static LifeCallAdapterFactory create() {
        return new LifeCallAdapterFactory(null);
    }

    public static LifeCallAdapterFactory create(Executor executor) {
        Utils.checkNotNull(executor, "executor == null");
        return new LifeCallAdapterFactory(executor);
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
                    String.format("%s return type must be parameterized as %s<Foo> or %s<? extends Foo>", RETURN_TYPE, RETURN_TYPE, RETURN_TYPE));
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

        final Executor executor = callbackExecutor != null ? callbackExecutor : retrofit.callbackExecutor();
        if (executor == null) throw new AssertionError();
        return new CallAdapter<Object, LifeCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public LifeCall<Object> adapt(Call<Object> call) {
                return new LifeCallbackCall<>(executor, call);
            }
        };
    }
}
