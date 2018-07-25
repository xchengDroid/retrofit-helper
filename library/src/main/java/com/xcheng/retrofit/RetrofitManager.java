package com.xcheng.retrofit;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例
 */
public final class RetrofitManager {
    //全局的Retrofit对象，volatile保证可见性，赋值后同步刷新到主内存
    private volatile static Retrofit sRetrofit;
    private static final Map<String, Retrofit> sRetrofitMap = new HashMap<>(4);

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

    /**
     * @return true代表已经被初始化了
     */
    public static boolean hasInit() {
        return sRetrofit != null;
    }

    public static <T> T create(Class<T> service) {
        return retrofit().create(service);
    }

    public static Retrofit retrofit() {
        if (sRetrofit == null) {
            throw new IllegalStateException("RetrofitManager must be init with retrofit before using.");
        }
        return sRetrofit;
    }

    public synchronized static void put(String name, Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        sRetrofitMap.put(name, retrofit);
    }

    public synchronized static Retrofit get(String name) {
        return sRetrofitMap.get(name);
    }

    public synchronized static void remove(String name) {
        sRetrofitMap.remove(name);
    }
}
