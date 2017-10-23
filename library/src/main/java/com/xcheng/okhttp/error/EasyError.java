package com.xcheng.okhttp.error;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * 通用的错误信息，可继承添加一些自定义的error
 * Created by chengxin on 2017/6/22.
 */
public class EasyError {
    private int code;
    private String message;

    public EasyError(int code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 静态方法创建简单的错误信息,
     * errorCode为{@link Integer#MIN_VALUE}
     *
     * @param message 错误描述
     * @return EasyError
     */
    public static EasyError create(String message) {
        return new EasyError(Integer.MIN_VALUE, message);
    }
    /**
     * 自定义的错误码
     *
     * @return code
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void log() {
        Log.e(getClass().getSimpleName(), toString());
    }

    @Override
    public String toString() {
        return "[ code: " + code + ", message: " + message + " ]";
    }
}
