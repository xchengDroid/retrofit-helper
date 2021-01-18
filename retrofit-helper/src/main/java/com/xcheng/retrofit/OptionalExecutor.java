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
    private final Handler handler = new Handler(Looper.getMainLooper());

    private OptionalExecutor() {
    }

    public static OptionalExecutor get() {
        return EXECUTOR;
    }

    /**
     * Executes the given task on the main thread.
     * <p>
     * If the current thread is a main thread, immediately runs the given runnable.
     *
     * @param runnable The runnable to run on the main thread.
     */
    public void executeOnMainThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }

    public void postToMainThread(@NonNull Runnable runnable) {
        handler.post(runnable);
    }
}
