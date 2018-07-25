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
    private volatile static RetrofitManager instance;
    //全局的retrofit对象
    private final Retrofit retrofit;
    //不同配置的retrofit集合，如url ,converter等
    private final Map<String, Retrofit> retrofitMap;

    private RetrofitManager(Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        this.retrofit = retrofit;
        this.retrofitMap = new HashMap<>(4);
    }

    /**
     * 创建全局的RetrofitManager实例
     *
     * @param retrofit 全局的retrofit实例
     * @return true代表创建成功  false代表失败
     */
    public static boolean create(Retrofit retrofit) {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    instance = new RetrofitManager(retrofit);
                    return true;
                }
            }
        }
        Log.e("RetrofitManager", "RetrofitManager had already been created before.");
        return false;
    }

    /**
     * @return 如果为null, 表示未初始化
     */
    public static RetrofitManager instance() {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    Log.e("RetrofitManager", "You need to call create(Retrofit) at least once to create the singleton");
                }
            }
        }
        return instance;
    }

    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    public Retrofit retrofit() {
        return retrofit;
    }

    /*全局保存不同配置的Retrofit,如 baseUrl不一样等*/
    public synchronized void putRetrofit(String tag, Retrofit retrofit) {
        Utils.checkNotNull(retrofit, "retrofit==null");
        retrofitMap.put(tag, retrofit);
    }

    public synchronized Retrofit getRetrofit(String tag) {
        return retrofitMap.get(tag);
    }

    public synchronized void removeRetrofit(String tag) {
        retrofitMap.remove(tag);
    }
    /*=====================end===========================*/

}
