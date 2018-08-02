package com.simple.okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.xcheng.retrofit.Call2;
import com.xcheng.retrofit.Callback2;
import com.xcheng.retrofit.ExecutorCallAdapterFactory;
import com.xcheng.retrofit.HttpError;
import com.xcheng.retrofit.Result;
import com.xcheng.retrofit.RetrofitManager;
import com.xcheng.retrofit.progress.ProgressInterceptor;
import com.xcheng.retrofit.progress.ProgressListener;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    ImageView imageView;
    public static final int BITMAP_ID = 12;
    Button btnProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.web_easyokhttp);
        imageView = findViewById(R.id.iv_easyokhttp);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.weather.com.cn/")
                .callFactory(new OkHttpClient.Builder()
                        .addInterceptor(ProgressInterceptor.INSTANCE)
                        .build())
                .addCallAdapterFactory(ExecutorCallAdapterFactory.INSTANCE)
                .build();
        RetrofitManager.getInstance().init(retrofit);

        RetrofitManager.getInstance().put("1", retrofit);
        RetrofitManager.getInstance().put("2", retrofit);
        RetrofitManager.getInstance().put("3", retrofit);
        RetrofitManager.getInstance().put(null, retrofit);
        btnProgress = findViewById(R.id.btn_progress);

//        OkHttpClient okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder())
//                .build();
        //   ProgressManager.getInstance().addResponseListener("", null);
        ProgressInterceptor.INSTANCE.setExecutor(retrofit.callbackExecutor());
        ProgressInterceptor.INSTANCE.registerListener(new ProgressListener("bitmap", true) {
            @Override
            protected void onProgress(long progress, long contentLength, boolean done) {
                btnProgress.setText(progress / (float) contentLength * 100 + "%");
            }
        });
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
        Call2<ResponseBody> call2 = RetrofitManager
                .create(Service.class)
                .gitHub();
        call2.enqueue("", new Callback2<ResponseBody>() {
            @NonNull
            @Override
            public Result<ResponseBody> parseResponse(Call2<ResponseBody> call2, Response<ResponseBody> response) {
                return Result.success(response.body());
            }

            @NonNull
            @Override
            public HttpError parseThrowable(Call2<ResponseBody> call2, Throwable t) {
                return new HttpError("123213");
            }

            @Override
            public void onError(Call2<ResponseBody> call2, HttpError error) {
                Log.e("print", error.msg);
            }

            @Override
            public void onSuccess(Call2<ResponseBody> call2, ResponseBody response) {
                try {
                    Log.e("print", response.string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void bitmap(final View view) {

        Call2<ResponseBody> call2 = RetrofitManager
                .create(Service.class)
                .getBitmap();

        call2.enqueue(hashCode(), new Callback2<ResponseBody>() {
            @Override
            public void onError(Call2<ResponseBody> call2, HttpError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                error.getCause().printStackTrace();
                error.printStackTrace();
            }

            @Override
            public void onSuccess(Call2<ResponseBody> call2, ResponseBody response) {
                Bitmap bitmap = BitmapFactory.decodeStream(response.byteStream());
                webView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ProgressInterceptor.INSTANCE.unregisterAll();
        super.onDestroy();
    }
}