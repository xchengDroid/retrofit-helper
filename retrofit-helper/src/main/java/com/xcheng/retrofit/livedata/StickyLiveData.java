package com.xcheng.retrofit.livedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chengxin on 2020/07/15.
 * 解决liveData发送粘性事件的问题,支持粘性和非粘性事件
 * https://www.v2ex.com/amp/t/603309 粘性事件的问题
 * https://www.jianshu.com/p/e08287ec62cd
 * https://www.jianshu.com/p/34348dd02ceb
 */
public class StickyLiveData<T> extends MutableLiveData<T> {
    private int mVersion = -1;
    private Map<Observer<? super T>, ObserverWrapper> mObservers = new LinkedHashMap<>();

    @Override
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        if (!mObservers.containsKey(observer)) {
            ObserverWrapper wrapper;
            super.observe(owner, wrapper = new ObserverWrapper(observer));
            mObservers.put(observer, wrapper);
        }
    }

    /**
     * 支持粘性事件
     */
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        super.observe(owner, observer);
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        if (!mObservers.containsKey(observer)) {
            ObserverWrapper wrapper;
            super.observeForever(wrapper = new ObserverWrapper(observer));
            mObservers.put(observer, wrapper);
        }
    }

    /**
     * 支持粘性事件
     */
    public void observeForeverSticky(@NonNull Observer<? super T> observer) {
        super.observeForever(observer);
    }

    @SuppressWarnings({"unchecked", "RedundantClassCall"})
    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        //observer instanceof ObserverWrapper
        //Illegal generic type for instanceof
        if (ObserverWrapper.class.isInstance(observer)) {
            super.removeObserver(observer);
            ObserverWrapper wrapper = (ObserverWrapper) observer;
            mObservers.remove(wrapper.mTarget);
        } else {
            ObserverWrapper wrapper = mObservers.get(observer);
            super.removeObserver(wrapper != null ? wrapper : observer);
            if (wrapper != null) {
                mObservers.remove(observer);
            }
        }
    }

    /**
     * Created by chengxin on 2020/07/15.
     * 代理Observer，拦截第一次注册时接收旧的事件
     * 静态内部类 T在 StickyLiveData已定义
     * like  java.util.ArrayList#SubList Map#EntrySet...
     */
    private class ObserverWrapper implements Observer<T> {
        private int mLastVersion;
        private Observer<? super T> mTarget;

        ObserverWrapper(@NonNull Observer<? super T> target) {
            mTarget = target;
            mLastVersion = mVersion;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (mLastVersion >= mVersion) {
                return;
            }
            mLastVersion = mVersion;
            mTarget.onChanged(t);
        }
    }
}
