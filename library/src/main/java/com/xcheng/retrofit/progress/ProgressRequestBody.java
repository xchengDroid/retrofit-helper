package com.xcheng.retrofit.progress;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Decorates an OkHttp request body to count the number of bytes written when writing it. Can
 * decorate any request body, but is most useful for tracking the upload inProgress of large
 * multipart requests.
 *
 * @author Leo Nikkilä
 */
class ProgressRequestBody extends RequestBody {
    private final RequestBody delegate;
    private final List<ProgressListener> listeners;

    ProgressRequestBody(RequestBody delegate, List<ProgressListener> listeners) {
        this.delegate = delegate;
        this.listeners = listeners;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(sink(sink));
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            private long bytesWritten = 0L;
            private long contentLength = -1L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
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
                                        listener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                                    }
                                });
                    } else {
                        listener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                    }
                }
            }
        };
    }
}