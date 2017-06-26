package com.xc.okhttp.error;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import okhttp3.Response;

/**
 * 通用的错误信息，可继承添加一些自定义的error
 * Created by chengxin on 2017/6/22.
 */
public class BaseError {
    private int errorCode;
    private String message;
    private Response responseNoBody;

    public BaseError(int errorCode, @NonNull String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static BaseError getNotFoundError(String message) {
        return new BaseError(Integer.MIN_VALUE, message);
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

    boolean isResponseError() {
        return responseNoBody != null;
    }

    /**
     * 返回没有body的 response,可以获取httpCode header request message 等
     *
     * @return rawResponseNoBody
     */
    @Nullable
    public Response getResponseNoBody() {
        return responseNoBody;
    }

    public void setResponseNoBody(Response responseNoBody) {
        this.responseNoBody = responseNoBody;
    }

    public int getCode() {
        if (isResponseError()) {
            return responseNoBody.code();
        }
        return -1;
    }

    @Override
    public String toString() {
        return "errorCode:" + errorCode + " ,message:" + message + " ,responseNoBody:" + responseNoBody;
    }
}
