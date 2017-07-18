package com.xcheng.okhttp.request;

import java.io.IOException;

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

    private RequestBody delegate;
    private Listener listener;
    private BufferedSink bufferedSink;

    ProgressRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
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
                listener.onRequestProgress(bytesWritten, contentLength, bytesWritten == contentLength);
            }
        };
    }

    interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength, boolean done);
    }
}