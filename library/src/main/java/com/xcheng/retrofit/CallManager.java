package com.xcheng.retrofit;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * 创建时间：2018/5/31
 * 编写人： chengxin
 * 功能描述：全局管理Call请求管理,just like {@link okhttp3.Dispatcher}
 */
public final class CallManager implements ActionManager<Call<?>> {
    @GuardedBy("this")
    private final List<CallTag> callTags = new ArrayList<>(4);
    private volatile static CallManager instance;

    private CallManager() {
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

    @Override
    public synchronized void add(Call<?> call, Object tag) {
        callTags.add(new CallTag(call, tag));
    }

    /**
     * 当call结束时移除
     *
     * @param call Retrofit Call
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Override
    public synchronized void remove(Call<?> call) {
        if (callTags.isEmpty())
            return;
        for (int index = 0; index < callTags.size(); index++) {
            if (call == callTags.get(index).call) {
                //like okhttp3.Headers#removeAll(String name)
                //remove(int index) 方法优于 remove(Object o)，无需再次遍历
                callTags.remove(index);
                break;
            }
        }
    }

    /**
     * 取消并移除对应tag的call，确保Call被取消后不再被引用，
     * 结合{@link #remove(Call)}方法双保险
     *
     * @param tag call对应的tag
     */
    @Override
    public synchronized void cancel(final @Nullable Object tag) {
        if (callTags.isEmpty())
            return;
        if (tag != null) {
            for (int index = 0; index < callTags.size(); index++) {
                CallTag callTag = callTags.get(index);
                if (callTag.tag.equals(tag)) {
                    callTag.call.cancel();
                    callTags.remove(index);
                    index--;
                }
            }
        } else {
            for (CallTag callTag : callTags) {
                callTag.call.cancel();
            }
            callTags.clear();
        }
    }

    @Override
    public synchronized boolean contains(Call<?> call) {
        for (CallTag callTag : callTags) {
            if (call == callTag.call) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存call和tag
     */
    final static class CallTag {
        private final Call<?> call;
        private final Object tag;

        CallTag(Call<?> call, Object tag) {
            Utils.checkNotNull(call == null, "call==null");
            Utils.checkNotNull(tag == null, "tag==null");
            this.call = call;
            this.tag = tag;
        }
    }
}
