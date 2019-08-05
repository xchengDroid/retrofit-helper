package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * just for android post UI thread
 */
public final class LifeCallAdapterFactory extends CallAdapter.Factory {
    private static final String RETURN_TYPE = "LifeCall";

    /**
     * 是否必须绑定生命周期，如果为true,当 {@code lifecycleProvider==null} 的时候回抛出异常
     */
    private final boolean checkProviderNonNull;

    /**
     * 默认是哪个生命周期事件释放请求
     */
    private final Lifecycle.Event defaultEvent;


    private LifeCallAdapterFactory(boolean checkProviderNonNull, Lifecycle.Event defaultEvent) {
        Utils.checkNotNull(defaultEvent, "defaultEvent==null");
        this.checkProviderNonNull = checkProviderNonNull;
        this.defaultEvent = defaultEvent;
    }

    /**
     * 如果用此构造函数，默认的executor为{@link Retrofit#callbackExecutor()}
     */
    public static LifeCallAdapterFactory create() {
        return new LifeCallAdapterFactory(false, Lifecycle.Event.ON_DESTROY);
    }

    public static LifeCallAdapterFactory create(boolean checkProviderNonNull, Lifecycle.Event defaultEvent) {
        return new LifeCallAdapterFactory(checkProviderNonNull, defaultEvent);
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

        OnLifecycleEvent annotation = Utils.findAnnotation(annotations, OnLifecycleEvent.class);
        final Lifecycle.Event event = annotation != null ? annotation.value() : defaultEvent;
        return new CallAdapter<Object, LifeCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public LifeCall<Object> adapt(Call<Object> call) {
                return new RealLifeCall<>(call, event, checkProviderNonNull);
            }
        };
    }
}
