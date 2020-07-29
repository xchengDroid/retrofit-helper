package com.xcheng.livedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

/**
 * 创建时间：2020-07-14
 * 编写人： chengxin
 * 功能描述：http通用的liveData
 */
public class ResultLiveData<T> extends StickyLiveData<Result<T>> {
    /**
     * 自动补全
     * 优先匹配参数范围小的方法
     */
    public void observe(@NonNull LifecycleOwner owner, @NonNull ResultObserver<T> observer) {
        super.observe(owner, observer);
    }

    public void setBody(@NonNull T value) {
        super.setValue(Result.body(value));
    }

    public void setMsg(@NonNull String msg) {
        super.setValue(Result.msg(msg));
    }

    public void postBody(@NonNull T value) {
        super.postValue(Result.body(value));
    }

    public void postMsg(@NonNull String msg) {
        super.postValue(Result.msg(msg));
    }

    @Nullable
    public T getBody() {
        Result<T> result = getValue();
        return result != null ? result.body : null;
    }

    /**
     * 重新发送缓存数据，避免多次请求
     *
     * @return true代表发送成功
     */
    public boolean setCacheBody() {
        if (getBody() != null) {
            setValue(getValue());
            return true;
        }
        return false;
    }
}
