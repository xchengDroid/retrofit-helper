package com.xcheng.retrofit;

import android.support.annotation.GuardedBy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * 创建时间：2018/5/31
 * 编写人： chengxin
 * 功能描述：全局管理http请求
 */
public class CallManager {
    @GuardedBy("this")
    private final List<CallWrap> callWraps;
    private volatile static CallManager instance;

    private CallManager() {
        callWraps = new ArrayList<>();
    }

    public static CallManager getInstance() {
        if (instance == null) {
            synchronized (CallManager.class) {
                if (instance == null) {
                    instance = new CallManager();
                }
            }
        }
        return instance;
    }

    synchronized void add(Call<?> call, Object tag) {
        callWraps.add(new CallWrap(call, tag));
    }

    synchronized void remove(Call<?> call) {
        for (CallWrap callWrap : callWraps) {
            if (call == callWrap.call) {
                callWraps.remove(callWrap);
                break;
            }
        }
    }

    public synchronized void cancel(Object tag) {
        for (CallWrap callWrap : callWraps) {
            if (callWrap.tag.equals(tag)) {
                callWrap.call.cancel();
            }
        }
    }

    public synchronized void cancelAll() {
        for (CallWrap callWrap : callWraps) {
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
