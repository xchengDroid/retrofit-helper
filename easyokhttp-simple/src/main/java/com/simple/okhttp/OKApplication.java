package com.simple.okhttp;

import android.app.Application;
import android.util.Log;

import com.xcheng.retrofit.ExecutorCallAdapterFactory;
import com.xcheng.retrofit.RetrofitManager;
import com.xcheng.retrofit.progress.ProgressInterceptor;
import com.xcheng.retrofit.progress.ProgressListener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * 创建时间：2018/8/2
 * 编写人： chengxin
 * 功能描述：
 */
public class OKApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.weather.com.cn/")
                .callFactory(new OkHttpClient.Builder()
                        .addInterceptor(new ProgressInterceptor(new ProgressListener() {
                            @Override
                            public void onUpload(String tag, long progress, long contentLength, boolean done) {
                                Log.e("print","onUpload:"+tag+"===progress:"+progress);
                            }

                            @Override
                            public void onDownload(String tag, long progress, long contentLength, boolean done) {
                                Log.e("print","onDownload:"+tag+"===progress:"+progress);
                            }
                        }))
                        .build())
                .addCallAdapterFactory(ExecutorCallAdapterFactory.INSTANCE)
                .build();
        RetrofitManager.init(retrofit);

//        Picasso.setSingletonInstance(new Picasso.Builder(this)
//                // .indicatorsEnabled(true)
//                .loggingEnabled(true)
//                .memoryCache(Cache.NONE)
//                .build());
//        if (Log.isLoggable("", Log.VERBOSE)) {
//            logWithTimeAndKey("Decoded from source", startTime);
//        }
        try {
            Class<?> clazz = Class.forName("com.squareup.picasso.Dispatcher");
            Field nameField = clazz.getDeclaredField("BATCH_DELAY");

            Field modifiersField = Field.class.getDeclaredField("modifiers"); //①
            modifiersField.setAccessible(true);
            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL); //②

            nameField.setAccessible(true); //这个同样不能少，除非上面把 private 也拿掉了，可能还得 public
            nameField.set(null, 0);
            Field nameField2 = clazz.getDeclaredField("BATCH_DELAY");

            //System.out.println(nameField2.get(null).); //输出 Shenzhen

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
