package com.simple.okhttp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonSyntaxException;
import com.xcheng.retrofit.BodyCallback;
import com.xcheng.retrofit.HttpError;
import com.xcheng.view.controller.ILoadingView;

import retrofit2.Call;

/**
 * Created by chengxin on 2017/9/24.
 */
public abstract class AnimCallback<T> extends BodyCallback<T> {
    private ILoadingView mLoadingView;

    public AnimCallback(@Nullable ILoadingView loadingView) {
        this.mLoadingView = loadingView;
    }

    @Override
    public void onStart(Call<T> call) {
        if (mLoadingView != null)
            mLoadingView.showLoading();
    }

    @NonNull
    @Override
    protected HttpError parseThrowable(Call<T> call, Throwable t) {
        if (t instanceof JsonSyntaxException) {
            return new HttpError("解析异常", t);
        }
        return super.parseThrowable(call, t);
    }

    @Override
    public void onCompleted(Call<T> call) {
        if (mLoadingView != null)
            mLoadingView.hideLoading();
    }
}
