package com.simple.okhttp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.simple.converter.GsonConverterFactory;
import com.xcheng.retrofit.HttpQueueAdapterFactory;
import com.xcheng.retrofit.LogInterceptor;
import com.xcheng.retrofit.ReplaceUrlCallFactory;
import com.xcheng.retrofit.RetrofitFactory;
import com.xcheng.view.EasyView;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
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
        EasyView.init(this);
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy
                .newBuilder()
                .tag("OKHTTP_LOG")
                .methodCount(1).showThreadInfo(false).build()) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });

        LogInterceptor logInterceptor = new LogInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Logger.d(message);
            }
        });
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(logInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wanandroid.com/")
                .callFactory(new ReplaceUrlCallFactory(client) {
                    @Nullable
                    @Override
                    protected HttpUrl getNewUrl(String baseUrlName, Request request) {
                        Log.e("print", "baseUrlName:" + baseUrlName);
                        if (baseUrlName.equals("baidu")) {
                            String oldUrl = request.url().toString();
                            String newUrl = oldUrl.replace("https://wanandroid.com/", "https://www.baidu.com/");
                            return HttpUrl.get(newUrl);
                        }
                        return null;
                    }
                })
                .addCallAdapterFactory(HttpQueueAdapterFactory.INSTANCE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //RetrofitManager.init(retrofit);
        RetrofitFactory.DEFAULT = retrofit;
    }
}
