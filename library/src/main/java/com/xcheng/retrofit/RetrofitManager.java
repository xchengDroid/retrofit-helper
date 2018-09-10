package com.xcheng.retrofit;

import android.support.annotation.GuardedBy;
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

    private static final String TAG = RetrofitManager.class.getSimpleName();

    private static final String LOG_INIT_RETROFIT = "Initialize RetrofitManager with retrofit success";
    private static final String WARNING_RE_INIT_RETROFIT = "Try to initialize RetrofitManager which had already been initialized before";
    private static final String ERROR_NOT_INIT = "RetrofitManager must be init with retrofit before using";
    /**
     * 全局的retrofit对象
     */
    private static Retrofit sRetrofit;
    /**
     * 缓存不同配置的retrofit集合，如url ,converter等
     */
    @GuardedBy("sRetrofitsCache")
    private static final Map<String, Retrofit> sRetrofitsCache = new HashMap<>(2);

    private RetrofitManager() {
    }

    /**
     * 初始化全局的Retrofit对象,like ARouter#LogisticsCenter,ImageLoader#ImageLoaderConfiguration
     *
     * @param retrofit 全局的Retrofit对象
     */
    public synchronized static void init(Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        if (sRetrofit == null) {
            Log.d(TAG, LOG_INIT_RETROFIT);
            sRetrofit = retrofit;
        } else {
            Log.e(TAG, WARNING_RE_INIT_RETROFIT);
        }
    }

    /**
     * like {@link retrofit2.OkHttpCall#cancel()}
     *
     * @return true if has init
     */
    public static boolean isInited() {
        if (sRetrofit == null) {
            synchronized (RetrofitManager.class) {
                //synchronized获得锁时会清空工作内存，从主内存重新获取最新数据
                //同步判断Retrofit是否已经初始化，防止此时正在同步块初始化
                return sRetrofit != null;
            }
        }
        return true;
    }

    public static <T> T create(Class<T> service) {
        return retrofit().create(service);
    }

    public static Retrofit retrofit() {
        if (!isInited()) {
            throw new IllegalStateException(ERROR_NOT_INIT);
        }
        return sRetrofit;
    }


    /**
     * 全局保存不同配置的Retrofit,如不同的baseUrl等
     *
     * @param tag      标记key
     * @param retrofit 对应的retrofit对象
     */
    public static void put(String tag, Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        synchronized (sRetrofitsCache) {
            sRetrofitsCache.put(tag, retrofit);
        }
    }

    public static Retrofit get(String tag) {
        synchronized (sRetrofitsCache) {
            return sRetrofitsCache.get(tag);
        }
    }

    public static void remove(String tag) {
        synchronized (sRetrofitsCache) {
            sRetrofitsCache.remove(tag);
        }
    }
}
