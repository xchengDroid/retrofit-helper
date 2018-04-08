package com.xcheng.retrofit;

public interface ProgressListener {
    void onProgress(long progress, long contentLength, boolean done);
}s