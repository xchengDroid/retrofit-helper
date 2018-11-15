package com.simple.converter;

import android.support.annotation.Nullable;

import com.xcheng.retrofit.FileResponseBodyConverter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：下载文件
 */
public class FileConverterFactory extends Converter.Factory {
    private final String filePath;

    public FileConverterFactory(String filePath) {
        this.filePath = filePath;
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type == File.class) {
            return new FileResponseBodyConverter(filePath);
        }
        return null;
    }
}
