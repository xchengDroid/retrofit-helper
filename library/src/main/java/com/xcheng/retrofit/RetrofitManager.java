package com.xcheng.retrofit;

import android.util.Log;

import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例
 */
public final class RetrofitManager {
    //全局的Retrofit对象
    private static Retrofit sRetrofit;
    private volatile static boolean sHasInit = false;

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
            sHasInit = true;
        } else {
            Log.e("RetrofitManager", "RetrofitManager had already been initialized before.");
        }
    }

    public static boolean hasInit() {
        return sHasInit;
    }

    public static <T> T create(Class<T> service) {
        if (!sHasInit) {
            throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
        }
        return sRetrofit.create(service);
    }

    public static Retrofit retrofit() {
        if (!sHasInit) {
            throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
        }
        return sRetrofit;
    }
}
