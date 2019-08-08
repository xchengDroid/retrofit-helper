package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.util.Log;

/**
 * 创建时间：2019-08-08
 * 编写人： chengxin
 * 功能描述：监听{@link LifeCall}被释放
 */
public interface OnDisposedListener {
    /**
     * 默认打印信息
     */
    OnDisposedListener DEFAULT = new OnDisposedListener() {
        @Override
        public void onDisposed(Call<?> call, Lifecycle.Event event) {
            if (RetrofitFactory.SHOW_LOG) {
                Log.d(Call.TAG, "disposed by-->" + event + ", " + call.request());
            }
        }
    };

    /**
     * 由于生命周期原因请求被取消了,此回调函数不保证与生命周期函数同步的
     */
    void onDisposed(Call<?> call, Lifecycle.Event event);
}
