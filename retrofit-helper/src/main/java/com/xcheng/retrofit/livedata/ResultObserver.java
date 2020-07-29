package com.xcheng.retrofit.livedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * 创建时间：2020-07-14
 * 编写人： chengxin
 * 功能描述：http 通用的Observer
 */
public abstract class ResultObserver<T> implements Observer<Result<T>> {
    @Override
    public final void onChanged(Result<T> result) {
        if (result != null) {
            onResult(result.body, result.msg);
        }
    }

    protected abstract void onResult(@Nullable T body, @NonNull String msg);
}
