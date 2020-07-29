package com.xcheng.retrofit;

import androidx.lifecycle.Lifecycle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 创建时间：2019-08-29
 * 编写人： chengxin
 * 功能描述：绑定生命周期，安全的切换到UI线程
 */
public final class SafeOnUiThread implements LifecycleProvider.Observer {

    private final LifecycleProvider provider;
    @Nullable
    private volatile Lifecycle.Event mEvent;

    public static SafeOnUiThread create(LifecycleProvider provider) {
        return new SafeOnUiThread(provider);
    }

    private SafeOnUiThread(LifecycleProvider provider) {
        this.provider = provider;
        provider.observe(this);
    }

    public void post(@NonNull Runnable action, Lifecycle.Event event) {
        postDelayed(action, event, 0);
    }

    /**
     * @param action the action to run on the UI thread
     * @param event  if {@code mEvent==event}, action will not be invoked
     */
    public void postDelayed(@NonNull final Runnable action, final Lifecycle.Event event, long delayMillis) {
        NetTaskExecutor.getInstance().postToMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (mEvent == event || mEvent == Lifecycle.Event.ON_DESTROY)
                    return;
                action.run();
            }
        }, delayMillis);
    }

    @Override
    public void onChanged(@NonNull Lifecycle.Event event) {
        this.mEvent = event;
        if (event == Lifecycle.Event.ON_DESTROY) {
            provider.removeObserver(this);
        }
    }
}
