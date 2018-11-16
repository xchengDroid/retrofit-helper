package com.simple.entity;

/**
 * 普通的结果提示 ，code=0代表成功
 * Created by chengxin on 2017/9/26.
 */
public class BaseResult<T> {
    private int code;
    private String msg;
    private T data;

    public T getData() {
        return data;
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
