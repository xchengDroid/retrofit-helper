package com.simple.okhttp;

import androidx.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import com.xcheng.retrofit.Call;
import com.xcheng.retrofit.Callback;
import com.xcheng.retrofit.HttpError;
import com.xcheng.view.controller.ILoadingView;

/**
 * Created by chengxin on 2017/9/24.
 */
public abstract class AnimCallback<T> implements Callback<T> {
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
        if (mLoadingView != null)
            mLoadingView.hideLoading();
        if (t != null) {
            HttpError filter;
            if (t instanceof JsonSyntaxException) {
                filter = new HttpError("解析异常", t);
            } else {
                filter = Callback.defaultConvert(t);
            }
            onError(call, filter);
        }
    }

    public abstract void onError(Call<T> call, HttpError error);
}
