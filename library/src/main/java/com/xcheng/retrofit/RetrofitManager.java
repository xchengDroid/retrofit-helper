package com.xcheng.retrofit;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：
 */
public class RetrofitManager {
    private static final List<CallWrap> CALL_WRAPS = new ArrayList<>();
    private static volatile RetrofitManager instance;
    private final Retrofit mRetrofit;

    private RetrofitManager(Retrofit retrofit) {
        this.mRetrofit = retrofit;
    }

    public static RetrofitManager create(@NonNull Retrofit retrofit) {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    instance = new RetrofitManager(retrofit);
                }
            }
        }
        return instance;
    }

    public static RetrofitManager instance() {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    throw new IllegalStateException("You need to call create(Retrofit) at least once to create the singleton");
                }
            }
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    public Retrofit retrofit() {
        return mRetrofit;
    }


    static void add(Call<?> call, Object tag) {
        synchronized (CALL_WRAPS) {
            CALL_WRAPS.add(new CallWrap(call, tag));
        }
    }

    static void remove(Call<?> call) {
        synchronized (CALL_WRAPS) {
            for (CallWrap callWrap : CALL_WRAPS) {
                if (call == callWrap.call) {
                    CALL_WRAPS.remove(callWrap);
                    return;
                }
            }
        }
    }

    private static List<CallWrap> callWraps() {
        synchronized (CALL_WRAPS) {
            return Utils.immutableList(CALL_WRAPS);
        }
    }

    public static void cancel(Object tag) {
        for (CallWrap callWrap : callWraps()) {
            if (callWrap.tag.equals(tag)) {
                callWrap.call.cancel();
            }
        }
    }

    public static void cancelAll(Object tag) {
        for (CallWrap callWrap : callWraps()) {
            callWrap.call.cancel();
        }
    }

    /**
     * 包裹Call和其tag
     */
    final static class CallWrap {
        private final Object tag;
        private final Call<?> call;

        CallWrap(Call<?> call, Object tag) {
            this.call = call;
            this.tag = tag;
        }
    }
}
