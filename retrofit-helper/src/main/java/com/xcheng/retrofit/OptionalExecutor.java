package com.xcheng.retrofit;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建时间：2019-09-11
 * 编写人： chengxin
 * 功能描述：默认的Executor，不做任何操作 like retrofit2.Platform
 */
public final class OptionalExecutor implements Executor {

    private static final OptionalExecutor EXECUTOR = new OptionalExecutor();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ExecutorService diskIO = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private static final String THREAD_NAME_STEM = "optional_disk_io_%d";
        private final AtomicInteger threadId = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(String.format(Locale.getDefault(), THREAD_NAME_STEM, threadId.getAndIncrement()));
            return t;
        }
    });

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
            mainHandler.post(runnable);
        }
    }

    /**
     * Executes the given task in the disk IO thread pool.
     *
     * @param runnable The runnable to run in the disk IO thread pool.
     */
    public void executeOnDiskIO(@NonNull Runnable runnable) {
        diskIO.execute(runnable);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }

    /**
     * Posts the given task to the main thread.
     *
     * @param runnable The runnable to run on the main thread.
     */
    public void postToMainThread(@NonNull Runnable runnable) {
        mainHandler.post(runnable);
    }

    public void postToMainThreadDelayed(@NonNull Runnable runnable, @Nullable LifecycleOwner owner, long delayMillis) {
        if (owner == null) {
            mainHandler.postDelayed(runnable, delayMillis);
        } else {
            mainHandler.postDelayed(() -> {
                //主线程判断是否已经销毁
                if (owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) {
                    runnable.run();
                }
            }, delayMillis);
        }
    }
}
