package com.xcheng.retrofit;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例
 */
public final class RetrofitManager {

    private static final String TAG = RetrofitManager.class.getSimpleName();

    private static final String LOG_INIT_RETROFIT = "Initialize RetrofitManager with retrofit success";
    private static final String WARNING_RE_INIT_RETROFIT = "Try to initialize RetrofitManager which had already been initialized before";
    private static final String ERROR_NOT_INIT = "RetrofitManager must be init with retrofit before using";
    /**
     * 全局的retrofit对象
     */
    private static volatile Retrofit sRetrofit;
    /**
     * 缓存不同配置的retrofit集合，如url ,converter等
     */
    private static final Map<String, Retrofit> sRetrofitsCache = new ConcurrentHashMap<>(2);

    private RetrofitManager() {
    }

    /**
     * 初始化全局的Retrofit对象,like Charset#bugLevel,HttpLoggingInterceptor#level,
     * AsyncTask#mStatus,facebook->stetho->LogRedirector#sLogger
     *
     * @param retrofit 全局的Retrofit对象
     */
    public static void init(Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        if (sRetrofit == null) {
            Log.d(TAG, LOG_INIT_RETROFIT);
            sRetrofit = retrofit;
        } else {
            Log.e(TAG, WARNING_RE_INIT_RETROFIT);
        }
    }

    /**
     * @return true if has init
     */
    public static boolean isInited() {
        //synchronized获得锁时会清空工作内存，从主内存重新获取最新数据
        //同步判断Retrofit是否已经初始化，防止此时正在同步块初始化
        return sRetrofit != null;
    }

    public static void destroy(boolean isAll) {
        sRetrofit = null;
        if (isAll) {
            sRetrofitsCache.clear();
        }
    }

    public static <T> T create(Class<T> service) {
        return retrofit().create(service);
    }

    public static Retrofit retrofit() {
        final Retrofit retrofit = sRetrofit;
        if (retrofit == null) {
            throw new IllegalStateException(ERROR_NOT_INIT);
        }
        return retrofit;
    }

    /**
     * 全局保存不同配置的Retrofit,如不同的baseUrl等
     *
     * @param tag      标记key
     * @param retrofit 对应的retrofit对象
     */
    public static void put(String tag, Retrofit retrofit) {
        sRetrofitsCache.put(tag, retrofit);
    }

    public static Retrofit get(String tag) {
        return sRetrofitsCache.get(tag);
    }

    public static void remove(String tag) {
        sRetrofitsCache.remove(tag);
    }
}
