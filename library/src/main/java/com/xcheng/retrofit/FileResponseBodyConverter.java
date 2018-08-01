package com.xcheng.retrofit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by cx on 2017/7/31.
 */
public class FileResponseBodyConverter implements Converter<ResponseBody, File> {
    /**
     * 标记Header的key ,如果header上有此键值对，尝试获取文件的路径
     */
    public static final String KEY_FILE_PATH = "File-Path";
    private final String path;

    public FileResponseBodyConverter(String path) {
        this.path = path;
    }

    @Override
    public File convert(ResponseBody value) throws IOException {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("failed to delete file:" + file.getPath());
        }

        File tmp = new File(file.getPath() + ".tmp");

        if (tmp.exists() && !tmp.delete()) {
            throw new IOException("failed to delete tmp file:" + tmp.getPath());
        }
        InputStream is = value.byteStream();
        FileOutputStream fos = null;
        try {
            if (!tmp.createNewFile()) {
                throw new IOException("failed to create file:" + tmp.getPath());
            }

            fos = new FileOutputStream(tmp);
            byte[] buffer = new byte[8096];
            int last = 0, c, ct = 0;
            while ((c = is.read(buffer)) != -1) {
                ct += c;
                fos.write(buffer, 0, c);
            }
            if (!tmp.renameTo(file)) {
                throw new IOException("failed to rename file:" + tmp.getPath());
            }
            return file;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        } finally {
            Utils.close(fos);
        }
    }
}
