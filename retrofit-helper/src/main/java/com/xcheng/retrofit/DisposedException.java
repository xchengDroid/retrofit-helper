package com.xcheng.retrofit;

/**
 * 创建时间：2019-08-06
 * 编写人： chengxin
 * 功能描述：当{@link LifeCall#isDisposed()}返回true时抛出的异常
 */
public class DisposedException extends RuntimeException {
    private static final long serialVersionUID = 7699927425836702496L;

    DisposedException(String message) {
        super(message);
    }

    DisposedException(String message, Throwable cause) {
        super(message, cause);
    }
}
