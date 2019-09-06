package com.xcheng.retrofit;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public abstract class ProgressResponseBody extends ResponseBody {
    private final ResponseBody delegate;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody delegate) {
        Utils.checkNotNull(delegate, "delegate==null");
        this.delegate = delegate;
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

    protected abstract void onDownload(long progress, long contentLength, boolean done);

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

                onDownload(totalBytesRead, contentLength, bytesRead == -1);
                return bytesRead;
            }
        };
    }
}