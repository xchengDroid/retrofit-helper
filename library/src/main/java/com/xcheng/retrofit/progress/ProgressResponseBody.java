package com.xcheng.retrofit.progress;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

class ProgressResponseBody extends ResponseBody {

    private final ResponseBody delegate;
    private final List<ProgressListener> listeners;
    private BufferedSource bufferedSource;

    ProgressResponseBody(ResponseBody delegate, List<ProgressListener> listeners) {
        this.delegate = delegate;
        this.listeners = listeners;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(delegate.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long contentLength = -1L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                final long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (contentLength == -1) {
                    //避免多次调用
                    contentLength = contentLength();
                }
                for (final ProgressListener listener : listeners) {
                    if (ProgressManager.getInstance().isOnMainThread()) {
                        ProgressManager.getInstance()
                                .getMainHandler()
                                .post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onProgress(totalBytesRead, contentLength, bytesRead == -1);
                                    }
                                });
                    } else {
                        listener.onProgress(totalBytesRead, contentLength, bytesRead == -1);
                    }
                }
                return bytesRead;
            }
        };
    }
}