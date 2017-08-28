package com.xcheng.okhttp.error;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * 通用的错误信息，可继承添加一些自定义的error
 * Created by chengxin on 2017/6/22.
 */
public class EasyError {
    private int errorCode;
    private String message;

    public EasyError(int errorCode, @NonNull String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static EasyError createDefaultError(String message) {
        return new EasyError(Integer.MIN_VALUE, message);
    }

    /**
     * 自定义的错误码
     *
     * @return errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
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
        return "[ errorCode: " + errorCode + ", message: " + message + " ]";
    }
}
