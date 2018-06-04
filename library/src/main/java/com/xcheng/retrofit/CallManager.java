package com.xcheng.retrofit;

import android.support.annotation.GuardedBy;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * 创建时间：2018/5/31
 * 编写人： chengxin
 * 功能描述：全局管理Call请求管理,just like {@link okhttp3.Dispatcher}
 */
public final class CallManager {
    @GuardedBy("this")
    private final List<CallTag> callTags;
    private volatile static CallManager instance;

    private CallManager() {
        callTags = new ArrayList<>(4);
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
        callTags.add(new CallTag(call, tag));
    }

    /**
     * 当call结束时移除
     *
     * @param call Retrofit Call
     */
    synchronized void remove(Call<?> call) {
        if (callTags.isEmpty())
            return;
        for (int index = 0; index < callTags.size(); index++) {
            if (call == callTags.get(index).call) {
                //remove(int index) 方法优于 remove(Object o)，无需再次遍历
                callTags.remove(index);
                break;
            }
        }
    }

    /**
     * 取消对应tag的call
     *
     * @param tag call对应的tag
     */
    public synchronized void cancel(Object tag) {
        if (callTags.isEmpty())
            return;
        for (CallTag callTag : callTags) {
            if (callTag.tag.equals(tag)) {
                callTag.call.cancel();
            }
        }
    }

    /**
     * 取消所有的call
     */
    public synchronized void cancelAll() {
        if (callTags.isEmpty())
            return;

        for (CallTag callTag : callTags) {
            callTag.call.cancel();
        }
    }

    /**
     * 保存call和tag
     */
    final static class CallTag {
        private final Call<?> call;
        private final Object tag;

        CallTag(Call<?> call, Object tag) {
            this.call = call;
            this.tag = tag;
        }
    }
}
