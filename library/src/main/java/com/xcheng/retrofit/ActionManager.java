package com.xcheng.retrofit;

import android.support.annotation.Nullable;

/**
 * 创建时间：2018/9/11
 * 编写人： chengxin
 * 功能描述：Action行为生命周期管理
 */
public interface ActionManager<Action> {

    /**
     * 添加某个Action
     */
    void add(Action action, Object tag);

    /**
     * 删除某个Action
     */
    void remove(Action action);

    /**
     * if tag!=null 取消tag一致的Action, else 取消所有请求
     */
    void cancel(@Nullable Object tag);

}