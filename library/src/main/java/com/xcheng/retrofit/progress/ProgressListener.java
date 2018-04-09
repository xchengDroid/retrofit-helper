package com.xcheng.retrofit.progress;

public interface ProgressListener {
    void onProgress(long progress, long contentLength, boolean done);
}