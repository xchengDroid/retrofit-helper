package com.simple.okhttp;

import com.simple.entity.Article;
import com.simple.entity.LoginInfo;
import com.simple.entity.WXArticle;
import com.xcheng.retrofit.LifeCall;

import java.io.File;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * 创建时间：2018/4/8
 * 编写人： chengxin
 * 功能描述：测试接口
 */
public interface ApiService {
    //登录
    //@CheckProvider(true)
    //@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @FormUrlEncoded
    @POST("user/login")
    LifeCall<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);

    //获取微信公众号列表
    @GET("wxarticle/chapters/json")
    LifeCall<List<WXArticle>> getWXarticle();

    //获取首页文章列表
    @GET("article/list/0/json")
    LifeCall<List<Article>> getArticle0();

    //下载文件
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    LifeCall<File> loadDouYinApk();

    //下载文件
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    LifeCall<File> lifeCycleTest();
}


