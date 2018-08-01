package com.simple.okhttp;

import com.xcheng.retrofit.Call2;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：
 */
public interface Service {
    @Headers({"Http-Progress: github",
            "User-Agent: Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Mobile Safari/537.36"})
    @GET("http://www.baidu.com/")
    Call2<ResponseBody> gitHub();

    @Headers({"Http-Progress: bitmap",
            "User-Agent: Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Mobile Safari/537.36"})
    @GET("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499010173&di=9599915fd6f9eb51f527cbbf62a84bd6&imgtype=jpg&er=1&src=http%3A%2F%2F4493bz.1985t.com%2Fuploads%2Fallimg%2F160119%2F5-16011Z92519.jpg")
    Call2<ResponseBody> getBitmap();
}


