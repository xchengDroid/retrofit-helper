package com.xcheng.retrofit;

import java.io.File;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by cx on 2017/7/31.
 * use {@link retrofit2.http.Streaming} instead
 */
@Deprecated
public class FileResponseBodyConverter implements Converter<ResponseBody, File> {
    private final String path;

    public FileResponseBodyConverter(String path) {
        this.path = path;
    }

    @Override
    public File convert(ResponseBody value) throws IOException {
        return Utils.writeToFile(value, path);
    }
}
