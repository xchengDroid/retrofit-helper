package com.simple.okhttp;

import com.xcheng.retrofit.Call2;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：
 */
public class MyCallAdapter implements CallAdapter<Object, Call2<?>> {
    private Type returnType;

    public MyCallAdapter(Type returnType) {

    }

    @Override
    public Type responseType() {
        return null;
    }

    @Override
    public Call2<?> adapt(Call<Object> call) {
        return null;
    }
}
