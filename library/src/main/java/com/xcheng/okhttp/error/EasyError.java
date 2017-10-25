package com.xcheng.okhttp.error;

/**
 * 通用的错误信息，可继承添加一些自定义的error
 * Created by chengxin on 2017/6/22.
 */
public class EasyError {
    private int code;
    private String message;
    //存放需要保存的对象，如原始的json等
    private Object result;

    public EasyError(int code, String message) {
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
     * 返回自定义的错误码
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean hasResult() {
        return result != null;
    }

    @Override
    public String toString() {
        return "[ code: " + code + ", message: " + message + " ]";
    }
}
