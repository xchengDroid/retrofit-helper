package com.xcheng.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import okhttp3.internal.Util;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：retrofit2工具类
 */
public class Utils {
    public static boolean isEmpty(Map<?, ?> map) {
        //因为它不能够确信加入到list中的值具有正确的类型。
        //意思是使用了一个未经处理的类型，它不能验证代码是类型安全的。
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 如果checkValue 为null返回defValue
     * like {@link org.json.JSONObject#optString(String, String)}
     * {@link android.os.Bundle#getString(String, String)}
     * {@link android.util.SparseIntArray#get(int, int)}
     * {@link android.content.res.TypedArray#getResourceId(int, int)}
     *
     * @return 不为空的对象
     */
    public static <T> T defValueIfNull(T checkValue, @NonNull T defValue) {
        return checkValue != null ? checkValue : defValue;
    }

    /**
     * Returns an immutable copy of {@code list}.
     */
    public static <T> List<T> immutableList(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "tr==null";
        }
        //like https://github.com/JakeWharton/timber/blob/master/timber/src/main/java/timber/log/Timber.kt
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        StringWriter sw = new StringWriter(256);
        PrintWriter pw = new PrintWriter(sw, false);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    @Nullable
    public static <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> cls) {
        //just in case
        if (annotations == null)
            return null;
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                //noinspection unchecked
                return (T) annotation;
            }
        }
        return null;
    }

    /**
     * Returns true if {@code annotations} contains an instance of {@code cls}.
     */
    static boolean isAnnotationPresent(Annotation[] annotations,
                                       Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) {
            if (cls.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

    public static File writeToFile(ResponseBody value, String path) throws IOException {
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
            int c;
            while ((c = is.read(buffer)) != -1) {
                fos.write(buffer, 0, c);
            }
            if (!tmp.renameTo(file)) {
                throw new IOException("failed to rename file:" + tmp.getPath());
            }
            return file;
        } finally {
            Util.closeQuietly(fos);
        }
    }
}
