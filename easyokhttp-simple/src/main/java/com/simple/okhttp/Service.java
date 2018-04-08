package com.simple.okhttp;

import com.xcheng.retrofit.Call2;

import okhttp3.ResponseBody;
import retrofit2.http.GET;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：
 */
public interface Service {
    @GET("https://github.com/")
    Call2<ResponseBody> gitHub();
}
