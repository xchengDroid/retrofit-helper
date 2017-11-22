package com.xcheng.okhttp.error;

import android.support.annotation.NonNull;

/**
 * 通用的错误信息，可继承添加一些自定义的error
 * Created by chengxin on 2017/6/22.
 */
public class EasyError {
    private static final String EMPTY_STR = "";
    /**
     * 唯一错误码
     */
    private final int code;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 存放需要保存的对象，如原始的json,表单出错后返回错误实体之类
     */
    private Object result;

    public EasyError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public EasyError(int code, String message, Object result) {
        this(code, message);
        this.result = result;
    }

    /**
     * 静态方法创建简单的错误信息,
     * code为{@link Integer#MIN_VALUE}
     *
     * @param message 错误描述
     * @return EasyError
     */
    public static EasyError create(String message) {
        return new EasyError(Integer.MIN_VALUE, message);
    }

    public int getCode() {
        return code;
    }

    @NonNull
    public String getMessage() {
        if (message == null) {
            return EMPTY_STR;
        }
        return message;
    }

    /**
     * 修改message描述
     *
     * @param message 错误描述
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{code="
                + code
                + ", message="
                + message
                + ", result="
                + result
                + '}';
    }
}
