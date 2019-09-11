package com.xcheng.retrofit;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * 创建时间：2019-09-11
 * 编写人： chengxin
 * 功能描述：默认的Executor，不做任何操作 like retrofit2.Platform
 */
final class OptionalExecutor implements Executor {

    private static final OptionalExecutor EXECUTOR = new OptionalExecutor();

    static OptionalExecutor get() {
        return EXECUTOR;
    }

    private OptionalExecutor() {
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }
}
