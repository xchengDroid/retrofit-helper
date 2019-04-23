package com.simple.okhttp;

/**
 * 普通的结果提示 ，code=0代表成功
 * Created by chengxin on 2017/9/26.
 */
public class Tip {
    private int code = -1;
    private String msg;

    public Tip(int code, String msg) {
        this.code = code;
        this.msg = msg;
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
