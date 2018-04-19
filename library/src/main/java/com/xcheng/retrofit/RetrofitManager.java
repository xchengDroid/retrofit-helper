package com.xcheng.retrofit;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：管理全局的Retrofit实例和所有的请求Call
 */
public class RetrofitManager {
    private static final List<CallWrap> CALL_WRAPS = new ArrayList<>();
    private static Retrofit sRetrofit;

    private RetrofitManager() {
        //no instance
    }

    public synchronized static void install(Retrofit retrofit) {
        if (retrofit == null) {
            throw new NullPointerException("retrofit==null");
        }
        if (sRetrofit == null) {
            sRetrofit = retrofit;
        } else {
            Log.e("RetrofitManager", "try to install retrofit which had already been installed before");
        }
    }

    public static Retrofit retrofit() {
        if (sRetrofit == null) {
            synchronized (RetrofitManager.class) {
                if (sRetrofit == null) {
                    throw new IllegalStateException("You need to call install(Retrofit) at least once");
                }
            }
        }
        return sRetrofit;
    }

    public static <T> T create(Class<T> service) {
        return retrofit().create(service);
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
