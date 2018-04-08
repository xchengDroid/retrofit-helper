package com.simple.okhttp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.xcheng.retrofit.Call2;
import com.xcheng.retrofit.Callback2;
import com.xcheng.retrofit.ExecutorCallAdapterFactory;
import com.xcheng.retrofit.HttpError;
import com.xcheng.retrofit.Result;
import com.xcheng.retrofit.RetrofitManager;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.weather.com.cn/")
                .addCallAdapterFactory(ExecutorCallAdapterFactory.INSTANCE)
                .build();
        RetrofitManager.create(retrofit);

    }

    public void json(View view) {
//        GetRequest getRequest = EasyOkHttp.get("/data/cityinfo/101010100.html").build();
//        ExecutorCall<Weather> okCall = new ExecutorCall<>(getRequest);
//        okCall.enqueue(new UICallback<Weather>() {
//            @Override
//            public void onError(OkCall<Weather> okCall, EasyError error) {
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSuccess(OkCall<Weather> okCall, Weather response) {
//                imageView.setVisibility(View.GONE);
//                webView.setVisibility(View.VISIBLE);
//                webView.loadData(new Gson().toJson(response), "text/html", "utf-8");
//            }
//        });
    }

    public void string(View view) {
        Call2<ResponseBody> call2 = RetrofitManager.instance().create(Service.class).gitHub();
        call2.cancel();
        call2.enqueue("", new Callback2<ResponseBody>() {
            @NonNull
            @Override
            public Result<ResponseBody> parseResponse(Call2<ResponseBody> call2, Response<ResponseBody> response) {
                return Result.success(response.body());
            }

            @NonNull
            @Override
            public Result<ResponseBody> parseThrowable(Call2<ResponseBody> call2, Throwable t) {
                return Result.error(new HttpError("123213"));
            }

            @Override
            public void onError(Call2<ResponseBody> call2, HttpError error) {

            }

            @Override
            public void onSuccess(Call2<ResponseBody> call2, ResponseBody response) {
                try {
                    Log.e("print", response.string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel(Call2<ResponseBody> call2) {
                super.onCancel(call2);
                Log.e("print","onCancel");
            }
        });
    }

    public void bitmap(final View view) {
//        GetRequest getRequest = EasyOkHttp.get("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499010173&di=9599915fd6f9eb51f527cbbf62a84bd6&imgtype=jpg&er=1&src=http%3A%2F%2F4493bz.1985t.com%2Fuploads%2Fallimg%2F160119%2F5-16011Z92519.jpg")
//                .outProgress()
//                .extra("type", "bitmap")
//                .build();
//        ExecutorCall<Bitmap> okCall = new ExecutorCall<>(getRequest);
//        okCall.enqueue(new UICallback<Bitmap>() {
//            @Override
//            public void onStart(OkCall<Bitmap> okCall) {
//                super.onStart(okCall);
//                Log.e("print", "before");
//            }
//
//            @Override
//            public void onFinish(OkCall<Bitmap> okCall) {
//                super.onFinish(okCall);
//                Log.e("print", "onAfter");
//            }
//
//            @Override
//            public void outProgress(OkCall<Bitmap> okCall, float progress, long total, boolean done) {
//                super.outProgress(okCall, progress, total, done);
//                TextView textView = (TextView) view;
//                textView.setText(progress * 100 + "%");
//                Log.e("print", "outProgress");
//            }
//
//            @Override
//            public void onError(OkCall<Bitmap> okCall, EasyError error) {
//                Log.e("print", "onError");
//
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSuccess(OkCall<Bitmap> okCall, Bitmap response) {
//                Log.e("print", "onSuccess");
//
//                webView.setVisibility(View.GONE);
//                imageView.setVisibility(View.VISIBLE);
//                imageView.setImageBitmap(response);
//            }
//        });
    }
}