package com.xcheng.retrofit;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * 创建时间：2019-09-11
 * 编写人： chengxin
 * 功能描述：默认的Executor，不做任何操作 like retrofit2.Platform
 */
public final class OptionalExecutor implements Executor {
    private static final OptionalExecutor EXECUTOR = new OptionalExecutor();
    private static final MainThreadExecutor MAIN_THREAD_EXECUTOR = new MainThreadExecutor();

    static Executor getExecutor() {
        return EXECUTOR;
    }

    public static Executor getMainThreadExecutor() {
        return MAIN_THREAD_EXECUTOR;
    }

    /**
     * 是否为主线程
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private OptionalExecutor() {
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }

    static final class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
