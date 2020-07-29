package com.simple.okhttp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import com.xcheng.retrofit.Call;
import com.xcheng.retrofit.DefaultCallback;
import com.xcheng.retrofit.DisposedException;
import com.xcheng.retrofit.HttpError;
import com.xcheng.view.controller.ILoadingView;

/**
 * Created by chengxin on 2017/9/24.
 */
public abstract class AnimCallback<T> extends DefaultCallback<T> {
    private ILoadingView mLoadingView;

    public AnimCallback(@Nullable ILoadingView loadingView) {
        this.mLoadingView = loadingView;
    }

    @Override
    public void onStart(Call<T> call) {
        if (mLoadingView != null)
            mLoadingView.showLoading();
    }

    @Override
    public void onCompleted(Call<T> call, @Nullable Throwable t) {
        super.onCompleted(call, t);
        if (DisposedException.isDestroyed(t)) {
            return;
        }
        if (mLoadingView != null)
            mLoadingView.hideLoading();
    }

    @NonNull
    @Override
    public HttpError parseThrowable(Call<T> call, Throwable t) {
        HttpError filterError;
        if (t instanceof JsonSyntaxException) {
            filterError = new HttpError("解析异常", t);
        } else {
            filterError = super.parseThrowable(call, t);
        }
        return filterError;
    }
}
