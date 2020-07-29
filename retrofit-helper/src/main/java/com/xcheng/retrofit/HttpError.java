package com.xcheng.retrofit;

import androidx.annotation.Nullable;

/**
 * 通用的错误信息，一般请求是失败只需要弹出一些错误信息即可,like{@link retrofit2.HttpException}
 * Created by chengxin on 2017/6/22.
 */
public final class HttpError extends RuntimeException {
    private static final long serialVersionUID = -134024482758434333L;
    /**
     * 展示在前端的错误描述信息
     */
    public String msg;

    /**
     * <p>
     * 请求失败保存失败信息,for example:
     * <li>BusiModel: {code:xxx,msg:xxx} 业务错误信息</li>
     * <li>original json:  原始的json</li>
     * <li>{@link retrofit2.Response}:错误响应体->Response<?></li>
     * <li>Throwable: 抛出的异常信息</li>
     * </p>
     */
    @Nullable
    public final transient Object body;

    public HttpError(String msg) {
        this(msg, null);
    }

    public HttpError(String msg, @Nullable Object body) {
        super(msg);
        if (body instanceof Throwable) {
            initCause((Throwable) body);
        }
        //FastPrintWriter#print(String str)
        this.msg = msg != null ? msg : "null";
        this.body = body;
    }

    /**
     * 保证和msg一致
     */
    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "HttpError {msg="
                + msg
                + ", body="
                + body
                + '}';
    }
}
