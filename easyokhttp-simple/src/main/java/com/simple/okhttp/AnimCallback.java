package com.simple.okhttp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import com.xcheng.retrofit.Call2;
import com.xcheng.retrofit.Callback2;
import com.xcheng.retrofit.HttpError;
import com.xcheng.view.controller.ILoadingView;

/**
 * Created by chengxin on 2017/9/24.
 */
public abstract class AnimCallback<T> extends Callback2<T> {
    private ILoadingView mLoadingView;

    public AnimCallback(@Nullable ILoadingView loadingView) {
        this.mLoadingView = loadingView;
    }

    @Override
    public void onStart(Call2<T> call2) {
        super.onStart(call2);
        if (mLoadingView != null)
            mLoadingView.showLoading();
    }

    @Override
    public void onCompleted(Call2<T> call2, @Nullable Call2.Cancel cancel) {
        super.onCompleted(call2, cancel);
        if (mLoadingView != null)
            mLoadingView.hideLoading();
    }

    @NonNull
    @Override
    public HttpError parseThrowable(Call2<T> call2, Throwable t) {
        HttpError filterError;
        if (t instanceof JsonSyntaxException) {
            filterError = new HttpError("解析异常", t);
        } else {
            filterError = super.parseThrowable(call2, t);
        }
        return filterError;
    }
}
