package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;

import java.io.IOException;

/**
 * 创建时间：2019-08-06
 * 编写人： chengxin
 * 功能描述：当{@link LifeCall#isDisposed()}返回true时抛出的异常
 */
public class DisposedException extends IOException {
    private static final long serialVersionUID = 7699927425836702496L;
    /**
     * 保存抛出异常前的最后一次生命周期事件
     */
    public final Lifecycle.Event lastEvent;

    DisposedException(Lifecycle.Event lastEvent) {
        super(getMessage(lastEvent));
        this.lastEvent = lastEvent;
    }

    DisposedException(Lifecycle.Event lastEvent, Throwable cause) {
        super(getMessage(lastEvent), cause);
        this.lastEvent = lastEvent;
    }

    private static String getMessage(Lifecycle.Event lastEvent) {
        return "Already disposed, lastEvent is " + lastEvent;
    }

    public boolean isDestroyed() {
        return lastEvent == Lifecycle.Event.ON_DESTROY;
    }
}
