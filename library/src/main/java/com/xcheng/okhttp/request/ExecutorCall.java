package com.xcheng.okhttp.request;

import com.google.gson.reflect.TypeToken;
import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.EasyPreconditions;
import com.xcheng.okhttp.util.Platform;

import java.io.IOException;
import java.util.List;

/**
 * UICallBack 回调主线程处理类
 * Created by chengxin on 2017/10/9.
 */
public class ExecutorCall<T> implements OkCall<T> {
    private static final Platform PLATFORM = Platform.get();
    private final RealCall<T> delegate;

    public ExecutorCall(OkRequest okRequest) {
        delegate = new RealCall<>(okRequest);
    }

    /**
     * for clone
     */
    private ExecutorCall(RealCall<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public OkResponse<T> execute() throws IOException {
        return delegate.execute();
    }

    @Override
    public void enqueue(final UICallback<T> uiCallback) {
        EasyPreconditions.checkNotNull(uiCallback, "uiCallback==null");
        delegate.setTokenIfNull(uiCallback.getClass());
        delegate.enqueue(new UICallback<T>() {
            @Override
            public void onBefore(OkCall<T> okCall) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPostUi()) {
                            uiCallback.onBefore(ExecutorCall.this);
                        }
                    }
                });
            }

            @Override
            public void inProgress(OkCall<T> okCall, final float progress, final long total, final boolean done) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPostUi()) {
                            uiCallback.inProgress(ExecutorCall.this, progress, total, done);
                        }
                    }
                });
            }

            @Override
            public void outProgress(OkCall<T> okCall, final float progress, final long total, final boolean done) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPostUi()) {
                            uiCallback.outProgress(ExecutorCall.this, progress, total, done);
                        }
                    }
                });
            }

            @Override
            public void onError(OkCall<T> okCall, final EasyError error) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPostUi()) {
                            uiCallback.onError(ExecutorCall.this, error);
                        }
                    }
                });
            }

            @Override
            public void onSuccess(OkCall<T> okCall, final T response) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isCanceled()) {
                            uiCallback.onSuccess(ExecutorCall.this, response);
                        } else {
                            onError(ExecutorCall.this, EasyError.create("Canceled"));
                        }
                    }
                });
            }

            @Override
            public void onAfter(OkCall<T> okCall) {
                PLATFORM.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPostUi()) {
                            uiCallback.onAfter(ExecutorCall.this);
                        }
                        RealCall.finished(delegate);
                    }
                });
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public OkCall<T> clone() {
        return new ExecutorCall<>(delegate.clone());
    }

    @Override
    public OkRequest request() {
        return delegate.request();
    }

    @Override
    public TypeToken<T> getTypeToken() {
        return delegate.getTypeToken();
    }


    @Override
    public boolean isPostUi() {
        return delegate.isPostUi();
    }

    public static List<RealCall<?>> getCalls() {
        return RealCall.getCalls();
    }
}
