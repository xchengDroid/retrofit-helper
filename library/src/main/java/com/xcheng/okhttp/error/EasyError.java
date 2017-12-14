package com.xcheng.okhttp.error;

import android.support.annotation.NonNull;

/**
 * 通用的错误信息，一般请求是失败只需要弹出一些错误信息即可
 * Created by chengxin on 2017/6/22.
 */
public class EasyError {
    /**
     * 展示在前端的错误描述信息
     */
    private String message;
    /**
     * 请求失败保存失败信息,如原始Exception、json、解析的错误实体
     */
    private final Object body;

    public EasyError(String message) {
        this(message, null);
    }

    public EasyError(String message, Object body) {
        this.message = message;
        this.body = body;
    }

    @NonNull
    public String getMessage() {
        if (message == null) {
            return "";
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

    /**
     * 强制装换之前注意确保对象类型正确
     */
    public Object getBody() {
        return body;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{message="
                + message
                + ", body="
                + body
                + "}";
    }
}
