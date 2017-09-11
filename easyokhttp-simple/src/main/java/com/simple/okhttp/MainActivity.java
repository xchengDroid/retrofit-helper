package com.simple.okhttp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xcheng.okhttp.EasyOkHttp;
import com.xcheng.okhttp.callback.BitmapParse;
import com.xcheng.okhttp.callback.JsonParse;
import com.xcheng.okhttp.callback.OkCall;
import com.xcheng.okhttp.callback.UICallback;
import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.request.GetRequest;
import com.xcheng.okhttp.request.OkConfig;
import com.xcheng.okhttp.request.OkHttpCall;

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
        OkConfig config = OkConfig.newBuilder()
                .okHttpClient(new OkHttpClient())
                .host("http://www.weather.com.cn/")
                .parseClass(JsonParse.class)
                .postUiIfCanceled(true)
                .build();
        EasyOkHttp.init(config);
    }

    public void json(View view) {
        GetRequest getRequest = new GetRequest.Builder().build();
                EasyOkHttp.get("/data/cityinfo/101010100.html")
                .parseClass(JsonParse.class).build();
        OkHttpCall<Weather> okCall = new OkHttpCall<>(getRequest);
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
        OkHttpCall<String> okCall = new OkHttpCall<>(getRequest);
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
                .parseClass(BitmapParse.class)
                .outProgress()
                .build();
        OkHttpCall<Bitmap> okCall = new OkHttpCall<>(getRequest);
        okCall.enqueue(new UICallback<Bitmap>() {

            @Override
            public void outProgress(OkCall<Bitmap> okCall, float progress, long total, boolean done) {
                super.outProgress(okCall, progress, total, done);
                TextView textView = (TextView) view;
                textView.setText(progress * 100 + "%");
                Log.e("print", progress + "==" + total + "==" + done);
            }

            @Override
            public void onError(OkCall<Bitmap> okCall, EasyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(OkCall<Bitmap> okCall, Bitmap response) {
                webView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(response);
            }
        });
    }
}