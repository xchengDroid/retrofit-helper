package com.simple.okhttp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;

import com.simple.entity.Article;
import com.simple.entity.LoginInfo;
import com.simple.entity.WXArticle;
import com.xcheng.retrofit.Call;
import com.xcheng.retrofit.DownloadCall;
import com.xcheng.retrofit.DownloadCallback;
import com.xcheng.retrofit.HttpError;
import com.xcheng.retrofit.RetrofitFactory;
import com.xcheng.retrofit.Utils;
import com.xcheng.view.controller.EasyActivity;
import com.xcheng.view.widget.ProgressView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;

public class MainActivity extends EasyActivity {
    ProgressView progressView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        progressView = findViewById(R.id.progress);
        progressView.setTextColor(Color.RED);
        progressView.setTextSize(30);
        progressView.setProgressViewTextGenerator(new ProgressView.ProgressViewTextGenerator() {
            @Override
            public String generateText(ProgressView progressBar, int value, int maxValue) {
                return String.valueOf(value) + "%";
            }
        });
    }

    public void login(View view) {
        //多个版本依赖冲突，debug源码引入导致乱跳
        //2.1.0才有的有参构造 这样api不一致可以看出打包到底是引入哪个依赖
        //  MutableLiveData<String> liveData = new MutableLiveData<>("123213");
        //2.0.0
        MutableLiveData<String> liveData = new MutableLiveData<>();
        liveData.setValue("1212");
        for (int index = 0; index < 1; index++) {
            RetrofitFactory.create(ApiService.class)
                    // .getLogin("singleman", "123456")
                    .getLogin("user/login", "singleman", "123456")
                    .enqueue(this, Lifecycle.Event.ON_PAUSE, new AnimCallback<LoginInfo>(this) {
                        @Override
                        public void onError(Call<LoginInfo> call2, HttpError error) {
                            Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess(Call<LoginInfo> call2, LoginInfo response) {
                            Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public void wxarticle(View view) {
        RetrofitFactory.create(ApiService.class)
                .getWXarticle()
                .enqueue(this, new AnimCallback<List<WXArticle>>(this) {
                    @Override
                    public void onError(Call<List<WXArticle>> call, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call<List<WXArticle>> call, List<WXArticle> response) {
                        Toast.makeText(MainActivity.this, "获取公众号列表成功", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void article0(View view) {
        RetrofitFactory.create(ApiService.class)
                .getArticle0()
                .enqueue(this, new AnimCallback<List<Article>>(this) {
                    @Override
                    public void onError(Call<List<Article>> call, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call<List<Article>> call, List<Article> response) {
                        Toast.makeText(MainActivity.this, "获取首页列表成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void progress(View view) {
        RetrofitFactory.create(ApiService.class)
                .getArticle0()
                .enqueue(this, new AnimCallback<List<Article>>(this) {
                    @Override
                    public void onError(Call<List<Article>> call2, HttpError error) {
                        Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(Call<List<Article>> call2, List<Article> response) {
                        Toast.makeText(MainActivity.this, "获取首页列表成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    int count;
    DownloadCall<File> call;

    public void download(View view) {
        final Button button = (Button) view;
        if (call != null) {
            call.cancel();
            call = null;
            button.setText("下载抖音apk");
            return;
        }
        button.setText("取消下载");
        final String filePath = new File(getContext().getExternalCacheDir(), "test_douyin.apk").getPath();

        RetrofitFactory.create(ApiService.class)
                .loadDouYinApk()
                .enqueue(new DownloadCallback<File>() {
                    @Nullable
                    @Override
                    public File convert(DownloadCall<File> call, ResponseBody value) throws IOException {
                        MainActivity.this.call = call;
                        return Utils.writeToFile(value, filePath);
                    }

                    @Override
                    public void onProgress(DownloadCall<File> call, long progress, long contentLength, boolean done) {
                        count++;
                        Log.e("print", "onDownLoad:" + count);
                        progressView.setProgress((int) (progress * 100f / contentLength), false);
                    }

                    @Override
                    public void onError(DownloadCall<File> call, Throwable t) {
                        progressView.setProgress(0);
                        Log.e("print", "", t);
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(DownloadCall<File> call, File file) {
                        button.setText("下载完成");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        login(null);
    }
}