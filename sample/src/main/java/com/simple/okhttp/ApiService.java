package com.simple.okhttp;

import com.simple.entity.Article;
import com.simple.entity.LoginInfo;
import com.simple.entity.RegisterInfo;
import com.simple.entity.WXArticle;
import com.xcheng.retrofit.Call;

import java.io.File;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.SkipCallbackExecutor;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：测试接口
 */
public interface ApiService {
    //登录
    //@SkipCallbackExecutor

    @FormUrlEncoded
    @POST("user/register")
    Call<RegisterInfo> register(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @Headers("BaseUrlName:baidu")
    @POST("user/login")
    Call<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("user/login")
    Call<LoginInfo> getLogin(@Header("BaseUrlName") /*动态替换*/String baseUrlName, @Field("username") String username, @Field("password") String password);

    //@SkipCallbackExecutor
    @Streaming
    @FormUrlEncoded
    @POST("user/login")
    retrofit2.Call<ResponseBody> getLoginTestOnMainThread(@Field("username") String username, @Field("password") String password);

    //获取微信公众号列表
    @GET("wxarticle/chapters/json")
    Call<List<WXArticle>> getWXarticle();

    //获取首页文章列表
    @GET("article/list/0/json")
    Call<List<Article>> getArticle0();

    //下载文件
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    Call<File> loadDouYinApk();

    //下载文件
    // @Streaming
    @SkipCallbackExecutor
    @Headers("LogLevel:BASIC")
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    retrofit2.Call<ResponseBody> loadDouYinApkTestOnMainThread();
}


