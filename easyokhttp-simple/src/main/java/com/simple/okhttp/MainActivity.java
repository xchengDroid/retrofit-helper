package com.simple.okhttp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.BitmapParser;
import com.xcheng.okhttp.callback.HttpParser;
import com.xcheng.okhttp.callback.JsonParser;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.ExecutorCall;
import com.xcheng.okhttp.request.GetRequest;
import com.xcheng.okhttp.request.OkCall;
import com.xcheng.okhttp.request.OkConfig;
import com.xcheng.okhttp.request.OkRequest;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    ImageView imageView;
    public static final int BITMAP_ID = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.web_easyokhttp);
        imageView = findViewById(R.id.iv_easyokhttp);
        OkConfig config = OkConfig.newBuilder()
                .client(new OkHttpClient())
                .baseUrl("http://www.weather.com.cn/")
                .parserFactory(new HttpParser.Factory() {
                    @NonNull
                    @Override
                    public HttpParser<?> parser(OkRequest request) {
                        if (request.id() == BITMAP_ID) {
                            return BitmapParser.INSTANCE;
                        }
                        return JsonParser.INSTANCE;
                    }
                })
                .postUiIfCanceled(true)
                .build();
        EasyOkHttp.init(config);
    }

    public void json(View view) {
        GetRequest getRequest = EasyOkHttp.get("/data/cityinfo/101010100.html").build();
        ExecutorCall<Weather> okCall = new ExecutorCall<>(getRequest);
        okCall.enqueue(new UICallback<Weather>() {
            @Override
            public void onError(OkCall<Weather> okCall, EasyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(OkCall<Weather> okCall, Weather response) {
                webView.loadData(new Gson().toJson(response), "text/html", "utf-8");
            }
        });
    }

    public void string(View view) {
        GetRequest getRequest = EasyOkHttp.get("https://github.com/").outProgress().build();
        ExecutorCall<String> okCall = new ExecutorCall<>(getRequest);
        okCall.enqueue(new UICallback<String>() {

            @Override
            public void onError(OkCall<String> okCall, EasyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(OkCall<String> okCall, String response) {
                imageView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadData(response, "text/html", "utf-8");
            }
        });
    }

    public void bitmap(final View view) {
        GetRequest getRequest = EasyOkHttp.get("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499010173&di=9599915fd6f9eb51f527cbbf62a84bd6&imgtype=jpg&er=1&src=http%3A%2F%2F4493bz.1985t.com%2Fuploads%2Fallimg%2F160119%2F5-16011Z92519.jpg")
                .outProgress()
                .id(BITMAP_ID)
                .build();
        ExecutorCall<Bitmap> okCall = new ExecutorCall<>(getRequest);
        okCall.enqueue(new UICallback<Bitmap>() {
            @Override
            public void onStart(OkCall<Bitmap> okCall) {
                super.onStart(okCall);
                Log.e("print", "before");
            }

            @Override
            public void onFinish(OkCall<Bitmap> okCall) {
                super.onFinish(okCall);
                Log.e("print", "onAfter");
            }

            @Override
            public void outProgress(OkCall<Bitmap> okCall, float progress, long total, boolean done) {
                super.outProgress(okCall, progress, total, done);
                TextView textView = (TextView) view;
                textView.setText(progress * 100 + "%");
                Log.e("print", "outProgress");
            }

            @Override
            public void onError(OkCall<Bitmap> okCall, EasyError error) {
                Log.e("print", "onError");

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(OkCall<Bitmap> okCall, Bitmap response) {
                Log.e("print", "onSuccess");

                webView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(response);
            }
        });
    }
}