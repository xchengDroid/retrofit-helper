retrofit-helper 

> Retrofit是很多android开发者都在使用的Http请求库！他负责网络请求接口的封装,底层实现是OkHttp,它的一个特点是包含了特别多注解，通过动态代理的方式使得开发者在使用访问网络的时候更加方便简单高效。



- #### 1. Retrofit-helper扩展了那些功能

  | 描述                             | 相关类和方法                                                 |
  | -------------------------------- | ------------------------------------------------------------ |
  | 丰富的回调接口                   | `onSuccess(Call<T> call, T response)`  `onError(Call<T> call, HttpError error)` `onStart(Call<T> call)`  和`onCompleted(Call<T> call, @Nullable Throwable t)`等 |
  | 动态替换retrofit 的baseUrl       | CallFactoryProxy 采用代理的方式灵活简单                      |
  | 绑定Activity或者Fragment生命周期 | `CompletableCall<T> enqueue(LifecycleOwner owner, Callback<T> callback)` |
  | 下载文件进度监听                 | `FileCallback` 监听文件下载、灵活易用，无需在`Interceptor`全局拦截 |
  | 单独指定某个请求的日志级别       | `LogInterceptor`支持单独指定日志级别，且避免多个请求并行导致日志错乱问题 |
  
- #### 2. 使用

  - 2.1  丰富回调接口

     监听开始、成功、失败、结束等。[点击查看详解](https://www.jianshu.com/p/aeea4fe91102)

    ```java
    public interface Callback<T> extends retrofit2.Callback<T> {
        /**
         * @param call The {@code Call} that was started
         */
        void onStart(Call<T> call);
    
        /**
         * @param call The {@code Call} that was completed
         */
        void onCompleted(Call<T> call);
    }
    ```
    
    使用如下，初始化全局retrofit在适当的位置，如Application的 onCreate方法
    
    ```java
    OkHttpClient client = new OkHttpClient.Builder()
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addCallAdapterFactory(CompletableCallAdapterFactory.INSTANCE)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    RetrofitFactory.DEFAULT = retrofit;
    ```
    
    发起普通请求：
    
    请求接口
    
    ```java
    @FormUrlEncoded
    @POST("user/login")
    CompletableCall<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```
    
    发起请求
    
    ```java
    RetrofitFactory.create(ApiService.class)
            .getLogin("xxxxxx", "123456")
            .enqueue(new BodyCallback<LoginInfo>() {
                @Override
                public void onStart(Call<LoginInfo> call) {
                    showLoading();
                }
    
                @Override
                protected void onError(Call<LoginInfo> call, HttpError error) {
                    Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
    
                @Override
                protected void onSuccess(Call<LoginInfo> call, LoginInfo loginInfo) {
                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                }
    
                @Override
                public void onCompleted(Call<LoginInfo> call, @Nullable Throwable t) {
                    hideLoading();
                }
            });
    ```
    
    ​       
    
  - 2.2 CompletableCall支持绑定生命周期，当触发指定的生命周期将不会执行回调方法，并且请求被取消。保证页面销毁等不会导致错误
  
    ```java
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        RetrofitFactory.create(ApiService.class)
                .getLogin("xxxxxx", "123456")
          			//传入LifeOwner 即当前的this 对象
                .enqueue(this, callback)
                
    ```
    
  - 2.3 动态替换retrofit 的baseUrl
  
     [点击查看原理](https://blog.csdn.net/issingleman/article/details/100542499)  并不是通过Interceptor 拦截器实现，而是采用代理CallFactory实现 `ReplaceUrlCallFactory`
  
     

  - 2.4  下载文件进度监听
  
    ​       在回调函数处监听下载进度，避免通过Interceptor监听影响灵活性。注意需要在调用FileCallback即可
  
    ```java
    //下载文件,大文件下防止内存溢出添加此注解
    @Streaming
    //防止打印文件导致内存溢出，单独设置日志级别
    @Headers("LogLevel:BASIC")
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    CompletableCall<ResponseBody> loadDouYinApk();
    ```
    
    ```java
    final String filePath = new File(getContext().getExternalCacheDir(), "test_douyin.apk").getPath();
    RetrofitFactory.create(ApiService.class)
            .loadDouYinApk()
            .enqueue(new FileCallback(true, 0.01f) {
                @Override
                protected void onStart() {
                    Log.e("print", "onStart:");
                }
    
                @Override
                protected void onCompleted() {
                  Log.e("print", "onCompleted:");
                }
    
                @Override
                protected void onResponse(File file) {
                    Log.e("print", "onResponse:");
    
                }
  
              @Override
              protected void onFailure(Throwable t) {
                    t.printStackTrace();
              }
    
              @Nullable
                @Override
              protected File onConvert(ResponseBody value) throws IOException {
                    Log.e("print", "onConvert:");
                	//自行对文件流进行处理
                    return Utils.writeToFile(value, filePath);
                }
    
                @Override
                protected void onProgress(long progress, long contentLength, boolean done) {
                    // Log.e("print", progress + "onDownLoad:" + contentLength + done);
                    if (done) {
                        Log.e("print", progress + "onDownLoad:" + contentLength);
                    }
                    progressView.setProgress((int) (progress * 100f / contentLength), false);
                    if (done) {
                        button.setText("下载完成");
                    }
                }
            });
    ```
    
    
    
  - 2.5  灵活的日志拦截`LogInterceptor`
  
  支持单独指定某个请求的日志级别，并且避免多线程情况下日志错乱的问题
  
    使用方式
  
    ```java
    FullLogInterceptor fullLogInterceptor = new FullLogInterceptor(new fullLogInterceptor.Logger() {
        @Override
        public void log(String message) {
            Logger.d(message);
      }
    });
    fullLogInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(fullLogInterceptor)
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addCallAdapterFactory(CallAdapterFactory.INSTANCE)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    RetrofitFactory.DEFAULT = retrofit;
    ```
    
  单独指定某个请求的日志级别 @Headers("LogLevel:NONE") 或 @Headers("LogLevel:BASIC") 或 @Headers("LogLevel:HEADERS") 或@Headers("LogLevel:BODY")
    
  ```java
    //  @Headers("LogLevel:NONE")
    //  @Headers("LogLevel:HEADERS")
    //  @Headers("LogLevel:BASIC")
        @Headers("LogLevel:BODY")
        @FormUrlEncoded
        @POST("user/login")
        CompletableCall<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```
    
  效果图
    
  ![日志](https://github.com/xchengDroid/retrofit-helper/blob/master/screenshots/logstyle.png)
  
  
- #### 3.注意事项

  - 4.1 构建retrofit是需要CompletableCallAdapterFactory实例，否则无法处理返回为Call的服务接口

  - 4.2 `Callback`的回调函数均在主线程执行，如果Call绑定了生命周期触发了`cancel()`方法

    UI回调方法均不会执行。
    
  
- #### 4.下载

  ```groovy
  //项目根目录build.gradle 添加仓库地址
   allprojects {
      repositories {
        	//.....
          maven { url "https://jitpack.io" }
      }
  }
  //引入依赖
  dependencies {
        implementation 'com.github.xchengDroid:retrofit-helper:3.2.1'
  }
  
  ```
  

​        github地址: [retrofit-helper](https://github.com/xchengDroid/retrofit-helper)

  

#### License

```
Copyright 2019 xchengDroid

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

