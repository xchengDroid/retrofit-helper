package com.xcheng.retrofit;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理网络相关线程的IO操作
 */
public final class NetTaskExecutor extends TaskExecutor {

    private final ExecutorService mDiskIO = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private static final String THREAD_NAME_STEM = "net_disk_io_%d";

        private final AtomicInteger mThreadId = new AtomicInteger(0);

        @SuppressLint("DefaultLocale")
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement()));
            return t;
        }
    });

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static volatile NetTaskExecutor sInstance;

    private NetTaskExecutor() {

    }

    /**
     * Returns an instance of the task executor.
     *
     * @return The singleton ArchTaskExecutor.
     */
    @NonNull
    public static NetTaskExecutor getInstance() {
        if (sInstance == null) {
            synchronized (NetTaskExecutor.class) {
                if (sInstance == null) {
                    sInstance = new NetTaskExecutor();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void executeOnDiskIO(@NonNull Runnable runnable) {
        mDiskIO.execute(runnable);
    }

    @Override
    public void postToMainThread(@NonNull Runnable runnable) {
        mMainHandler.post(runnable);
    }

    @Override
    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
