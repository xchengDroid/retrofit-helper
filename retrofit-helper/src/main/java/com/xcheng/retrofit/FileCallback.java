package com.xcheng.retrofit;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 监听下载进度 {@link Callback}
 * <p>
 * 下载文件
 *
 * @Streaming
 * @SkipCallbackExecutor
 * @Headers("LogLevel:BASIC")
 * @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk") Call<ResponseBody> loadDouYinApk();
 **/
@SuppressWarnings("JavaDoc")
public abstract class FileCallback implements Callback<ResponseBody> {
    public final boolean postToMain;
    public final float increaseOfProgress;

    public FileCallback(boolean postToMain, float increaseOfProgress) {
        this.postToMain = postToMain;
        this.increaseOfProgress = increaseOfProgress;
    }

    @Override
    public final void onStart(Call<ResponseBody> call) {
        post(() -> onStart());
    }

    @Override
    public final void onCompleted(Call<ResponseBody> call) {
        post(() -> onCompleted());
    }

    @Override
    public final void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        boolean signalledCallback = false;
        try {
            final ResponseBody body = response.body();
            if (body == null) {
                signalledCallback = true;
                post(() -> onFailure(new IllegalArgumentException("下载异常")));
                return;
            }
            ResponseBody responseBody = new ProgressResponseBody(body) {
                long lastProgress;

                @Override
                protected void onDownload(long progress, long contentLength, boolean done) {
                    if (progress - lastProgress > increaseOfProgress * contentLength || done) {
                        lastProgress = progress;
                        post(() -> onProgress(progress, contentLength, done));
                    }
                }
            };
            File file = onConvert(responseBody);
            signalledCallback = true;
            if (file != null) {
                post(() -> onResponse(file));
            } else {
                post(() -> onFailure(new IllegalArgumentException("下载异常")));
            }
        } catch (Throwable t) {
            if (!signalledCallback) {
                post(() -> onFailure(t));
            }
        }
    }

    @Override
    public final void onFailure(Call<ResponseBody> call, Throwable t) {
        post(() -> onFailure(t));
    }

    public void post(Runnable runnable) {
        if (postToMain) {
            OptionalExecutor.get().postToMainThread(runnable);
        } else {
            runnable.run();
        }
    }

    protected abstract void onStart();

    protected abstract void onCompleted();

    protected abstract void onResponse(File file);

    protected abstract void onFailure(Throwable t);

    @Nullable
    @WorkerThread
    protected abstract File onConvert(ResponseBody value) throws IOException;

    protected abstract void onProgress(long progress, long contentLength, boolean done);

}