package com.xcheng.retrofit;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例和所有的请求Call,面向过程编程
 */
public final class RetrofitManager {
    // 全部保存管理所有的Call,用于取消请求时遍历
    // like EventBust#eventTypesCache
    private static final List<CallWrap> sCallWraps = new ArrayList<>();
    //全局的Retrofit对象
    private static Retrofit sRetrofit;

    private RetrofitManager() {
    }

    /**
     * 初始化全局的Retrofit对象,like ARouter#LogisticsCenter,ImageLoader#ImageLoaderConfiguration
     *
     * @param retrofit 全局{@link Retrofit}对象
     */
    public static synchronized void init(Retrofit retrofit) {
        if (sRetrofit == null) {
            Utils.checkNotNull(retrofit, "retrofit==null");
            sRetrofit = retrofit;
        } else {
            Log.e("RetrofitManager", "RetrofitManager had already been initialized before.");
        }
    }

    private static void checkRetrofit() {
        if (sRetrofit == null) {
            synchronized (RetrofitManager.class) {
                if (sRetrofit == null) {
                    throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
                }
            }
        }
    }

    public static <T> T create(Class<T> service) {
        checkRetrofit();
        return sRetrofit.create(service);
    }

    public static Retrofit retrofit() {
        checkRetrofit();
        return sRetrofit;
    }


    static void add(Call<?> call, Object tag) {
        synchronized (sCallWraps) {
            sCallWraps.add(new CallWrap(call, tag));
        }
    }

    static void remove(Call<?> call) {
        synchronized (sCallWraps) {
            for (CallWrap callWrap : sCallWraps) {
                if (call == callWrap.call) {
                    sCallWraps.remove(callWrap);
                    break;
                }
            }
        }
    }

    private static List<CallWrap> callWraps() {
        synchronized (sCallWraps) {
            return Utils.immutableList(sCallWraps);
        }
    }


    public static void cancel(Object tag) {
        for (CallWrap callWrap : callWraps()) {
            if (callWrap.tag.equals(tag)) {
                callWrap.call.cancel();
            }
        }
    }

    public static void cancelAll() {
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
