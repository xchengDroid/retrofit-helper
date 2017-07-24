package com.xcheng.okhttp.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Executor;

public class Platform implements Executor {
    private static final Platform PLATFORM = new Platform();
    private final Handler UIHandler = new Handler(Looper.getMainLooper());

    private Platform() {
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if (isUiThread()) {
            command.run();
        } else {
            UIHandler.post(command);
        }
    }

    public static Platform get() {
        Log.e(Platform.class.getSimpleName(), PLATFORM.getClass().toString());
        return PLATFORM;
    }

    public static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
