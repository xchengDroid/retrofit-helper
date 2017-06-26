package com.simple.okhttp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xc.okhttp.EasyOkHttp;
import com.xc.okhttp.callback.BitmapParse;
import com.xc.okhttp.callback.JsonParse;
import com.xc.okhttp.callback.StringParse;
import com.xc.okhttp.callback.UICallback;
import com.xc.okhttp.error.BaseError;
import com.xc.okhttp.request.GetRequest;
import com.xc.okhttp.request.OKHttpCall;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.web_easyokhttp);
        imageView = (ImageView) findViewById(R.id.iv_easyokhttp);
        EasyOkHttp.init(new OkHttpClient(), "http://www.weather.com.cn/", StringParse.class, false);
    }

    public void json(View view) {
        GetRequest getRequest = EasyOkHttp.get("data/cityinfo/101010100.html").responseParse(JsonParse.class).build();
        OKHttpCall<Weather> okCall = new OKHttpCall<>(getRequest);
        okCall.enqueue(new UICallback<Weather>() {
            @Override
            public void onError(@NonNull BaseError error, int id) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess(@NonNull Weather response, int id) {
                webView.loadData(new Gson().toJson(response), "text/html", "utf-8");
            }
        });
    }

    public void string(View view) {
        GetRequest getRequest = EasyOkHttp.get("https://github.com/").build();
        OKHttpCall<String> okCall = new OKHttpCall<>(getRequest);
        okCall.enqueue(new UICallback<String>() {
            @Override
            public void onError(@NonNull BaseError error, int id) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess(@NonNull String response, int id) {
                imageView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.loadData(response, "text/html", "utf-8");
            }
        });
    }

    public void bitmap(View view) {
        GetRequest getRequest = EasyOkHttp.get("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499010173&di=9599915fd6f9eb51f527cbbf62a84bd6&imgtype=jpg&er=1&src=http%3A%2F%2F4493bz.1985t.com%2Fuploads%2Fallimg%2F160119%2F5-16011Z92519.jpg").responseParse(BitmapParse.class).build();
        OKHttpCall<Bitmap> okCall = new OKHttpCall<>(getRequest);
        okCall.enqueue(new UICallback<Bitmap>() {
            @Override
            public void onError(@NonNull BaseError error, int id) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull Bitmap response, int id) {
                webView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(response);
            }
        });
    }
}