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
import com.xcheng.retrofit.HttpError;
import com.xcheng.retrofit.HttpLoggingInterceptor;
import com.xcheng.retrofit.Result;
import com.xcheng.retrofit.RetrofitManager;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

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

        btnProgress = findViewById(R.id.btn_progress);
    }

    public void json(View view) {
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
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.valueOf("NONE");
        Call2<ResponseBody> call2 = RetrofitManager
                .create(Service.class)
                .getBitmap();

        call2.enqueue(hashCode(), new Callback2<ResponseBody>() {
            @Override
            public void onError(Call2<ResponseBody> call2, HttpError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                error.getCause().printStackTrace();
//                error.printStackTrace();
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

}