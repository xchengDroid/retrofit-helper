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

    private static final String LOG_INIT_RETROFIT = "Initialize RetrofitManager with retrofit";
    private static final String WARNING_RE_INIT_RETROFIT = "Try to initialize RetrofitManager which had already been initialized before";
    private static final String ERROR_NOT_INIT = "RetrofitManager must be init with retrofit before using";

    private volatile static RetrofitManager instance;
    /**
     * 全局的retrofit对象
     */
    private Retrofit retrofit;

    /**
     * 不同配置的retrofit集合，如url ,converter等
     */
    @GuardedBy("retrofitMap")
    private final Map<String, Retrofit> retrofitMap = new HashMap<>(4);

    private RetrofitManager() {
    }

    public static RetrofitManager getInstance() {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    instance = new RetrofitManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化全局的Retrofit对象,like ARouter#LogisticsCenter,ImageLoader#ImageLoaderConfiguration
     *
     * @param retrofit 全局{@link Retrofit}对象
     */
    public synchronized void init(Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        if (this.retrofit == null) {
            Log.d(TAG, LOG_INIT_RETROFIT);
            this.retrofit = retrofit;
        } else {
            Log.e(TAG, WARNING_RE_INIT_RETROFIT);
        }
    }

    /**
     * @return true 代表已经被初始化{@link #init(Retrofit)}
     */
    public boolean isInited() {
        if (retrofit == null) {
            synchronized (RetrofitManager.class) {
                return retrofit != null;
            }
        }
        return true;
    }

    public static <T> T create(Class<T> service) {
        return getInstance().retrofit().create(service);
    }

    public Retrofit retrofit() {
        if (!isInited()) {
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
    public void put(String tag, Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        synchronized (retrofitMap) {
            retrofitMap.put(tag, retrofit);
        }
    }

    public Retrofit get(String tag) {
        synchronized (retrofitMap) {
            return retrofitMap.get(tag);
        }
    }

    public void remove(String tag) {
        synchronized (retrofitMap) {
            retrofitMap.remove(tag);
        }
    }
}
