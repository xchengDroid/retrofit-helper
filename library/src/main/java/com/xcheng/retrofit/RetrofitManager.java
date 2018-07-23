package com.xcheng.retrofit;

import android.util.Log;

import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例
 */
public final class RetrofitManager {
    //全局的Retrofit对象，volatile 保证赋值后同步刷新到内存
    private volatile static Retrofit sRetrofit;

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

    public static boolean hasInit() {
        return sRetrofit != null;
    }

    public static <T> T create(Class<T> service) {
        if (!hasInit()) {
            throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
        }
        return sRetrofit.create(service);
    }

    public static Retrofit retrofit() {
        if (!hasInit()) {
            throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
        }
        return sRetrofit;
    }
}
