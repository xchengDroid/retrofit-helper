package com.xcheng.retrofit;

import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例,外观模式
 */
public final class RetrofitFactory {
    static final String TAG = "Retrofit";
    /**
     * 全局的Retrofit对象,like Charset#bugLevel,HttpLoggingInterceptor#level,
     * AsyncTask#mStatus,facebook->stetho->LogRedirector#sLogger,Timber->forestAsArray
     * CopyOnWriteArrayList==
     * BufferedInputStream#buf,in
     */
    public static volatile Retrofit DEFAULT;
    public static volatile Retrofit OTHER;

    private RetrofitFactory() {
        throw new AssertionError("No instances.");
    }

    public static <T> T create(Class<T> service) {
        //确保多线程的情况下retrofit不为空或者被修改了
        Retrofit retrofit = DEFAULT;
        if (retrofit == null) {
            throw new IllegalStateException("DEFAULT == null");
        }
        return retrofit.create(service);
    }

    public static <T> T other(Class<T> service) {
        Retrofit retrofit = OTHER;
        if (retrofit == null) {
            throw new IllegalStateException("OTHER == null");
        }
        return retrofit.create(service);
    }
}
