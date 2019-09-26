retrofit-helper 

> Retrofit是很多android开发者都在使用的Http请求库！他负责网络请求接口的封装,底层实现是OkHttp,它的一个特点是包含了特别多注解，通过动态代理的方式使得开发者在使用访问网络的时候更加方便简单高效。



- #### 1. Retrofit-helper扩展了那些功能

  | 描述                             | 相关类和方法                                                 |
  | -------------------------------- | ------------------------------------------------------------ |
  | 丰富的回调接口                   | `onSuccess(Call<T> call, T response)`  `onError(Call<T> call, HttpError error)` `onStart(Call<T> call)`  和`onCompleted(Call<T> call, @Nullable Throwable t)`等 |
  | 动态替换retrofit 的baseUrl       | CallFactoryProxy 采用代理的方式灵活简单                      |
  | 绑定Activity或者Fragment生命周期 | `LifeCall<T> bindToLifecycle(LifecycleProvider provider, Lifecycle.Event event)` |
  | 下载文件进度监听                 | `DownloadCall` 、`DownloadCallback`灵活易用，无需在`Interceptor`全局拦截 |
  | 单独指定某个请求的日志级别       | `FullLoggingInterceptor`支持单独指定日志级别，且避免多个请求并行导致日志错乱问题 |
  
- #### 2. 使用

  - 2.1  丰富回调接口

     监听开始、成功、失败、结束等。[点击查看详解](https://www.jianshu.com/p/aeea4fe91102)

    ```java
    public interface Callback<T> {
        /**
         * @param call The {@code Call} that was started
         */
        void onStart(Call<T> call);
    
        /**
         * @param call The {@code Call} that has thrown exception
         * @param t    统一解析throwable对象转换为HttpError对象，如果throwable为{@link HttpError}
         *             <li>则为{@link retrofit2.Converter#convert(Object)}内抛出的异常</li>
         *             如果为{@link retrofit2.HttpException}
         *             <li>则为{@link Response#body()}为null的时候抛出的</li>
         */
        @NonNull
        HttpError parseThrowable(Call<T> call, Throwable t);
    
        /**
         * 过滤一次数据,如剔除List中的null等,默认可以返回t
         */
        @NonNull
        T transform(Call<T> call, T t);
    
        void onError(Call<T> call, HttpError error);
    
        void onSuccess(Call<T> call, T t);
    
        /**
         * @param t 请求失败的错误信息
         */
        void onCompleted(Call<T> call, @Nullable Throwable t);
    }
    ```
    
    使用如下，初始化全局retrofit在适当的位置，如Application的 onCreate方法
    
    ```java
    OkHttpClient client = new OkHttpClient.Builder()
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addCallAdapterFactory(CallAdapterFactory.INSTANCE)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    RetrofitFactory.DEFAULT = retrofit;
    ```
    
    发起普通请求：
    
    请求接口
    
    ```java
    @FormUrlEncoded
    @POST("user/login")
    Call<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```
    
    发起请求
    
    ```java
    RetrofitFactory.create(ApiService.class)
            .getLogin("xxxxxx", "123456")
            .enqueue(new DefaultCallback<LoginInfo>() {
                @Override
                public void onStart(Call<LoginInfo> call) {
                    showLoading();
                }
    
                @Override
                public void onError(Call<LoginInfo> call, HttpError error) {
                    Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                }
    
                @Override
                public void onSuccess(Call<LoginInfo> call, LoginInfo loginInfo) {
                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                }
    
                @Override
                public void onCompleted(Call<LoginInfo> call, @Nullable Throwable t) {
                    hideLoading();
                }
            });
    ```
    
    ​       
    
  - 2.2 LifeCall支持绑定生命周期，当触发指定的生命周期将不会执行回调方法，并且请求被取消。保证页面销毁等不会导致错误

    ```java
    LifecycleProvider provider = AndroidLifecycle.createLifecycleProvider(this);
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        RetrofitFactory.create(ApiService.class)
                .getLogin("xxxxxx", "123456")
                .bindToLifecycle(provider, Lifecycle.Event.ON_DESTROY)
                .enqueue(new DefaultCallback<LoginInfo>() {
                    @Override
                    public void onStart(Call<LoginInfo> call) {
                        showLoading();
                    }
    
                    @Override
                    public void onError(Call<LoginInfo> call, HttpError error) {
                        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
    
                    @Override
                    public void onSuccess(Call<LoginInfo> call, LoginInfo loginInfo) {
                        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
    
                    @Override
                    public void onCompleted(Call<LoginInfo> call, @Nullable Throwable t){
                        hideLoading();
                    }
                });
    ```

  - 2.3 动态替换retrofit 的baseUrl

     [点击查看原理](https://blog.csdn.net/issingleman/article/details/100542499)  并不是通过Interceptor 拦截器实现，而是采用代理CallFactory实现 `ReplaceUrlCallFactory`

     

  - 2.4  下载文件进度监听

    ​       在回调函数处监听下载进度，避免通过Interceptor监听影响灵活性。注意需要在构建retrofit是添加 `DownloadCallAdapterFactory.INSTANCE`

    ```java
    //日志框架，可支持单独设置某个请求的日志级别，下文有详解
    FullLoggingInterceptor fullLoggingInterceptor = new FullLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            Logger.d(message);
        }
    });
    
    fullLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(fullLoggingInterceptor)
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addCallAdapterFactory(CallAdapterFactory.INSTANCE)
            .addCallAdapterFactory(DownloadCallAdapterFactory.INSTANCE)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    RetrofitFactory.DEFAULT = retrofit;
    ```

    ```java
    //下载文件,大文件下防止内存溢出添加此注解
    @Streaming
    //防止打印文件导致内存溢出，单独设置日志级别
    @Headers("LogLevel:BASIC")
    @GET("http://shouji.360tpcdn.com/181115/4dc46bd86bef036da927bc59680f514f/com.ss.android.ugc.aweme_330.apk")
    DownloadCall<File> loadDouYinApk();
    ```

    ```java
    final String filePath = new File(getContext().getExternalCacheDir(), "test_douyin.apk").getPath();
    RetrofitFactory.create(ApiService.class)
            .loadDouYinApk()
            .enqueue(new DownloadCallback<File>() {
                @Nullable
                @Override
                public File convert(DownloadCall<File> call, ResponseBody value) throws IOException {
                    return Utils.writeToFile(value, filePath);
                }
    
                @Override
                public void onProgress(DownloadCall<File> call, long progress, long contentLength, boolean done) {
                    progressView.setProgress((int) (progress * 100f / contentLength), false);
                }
    
                @Override
                public void onError(DownloadCall<File> call, Throwable t) {
                    progressView.setProgress(0);
                    Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                }
    
                @Override
                public void onSuccess(DownloadCall<File> call, File file) {
                    Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                }
            });
    ```

    

  - 2.5  灵活的日志拦截`FullLoggingInterceptor`

    支持单独指定某个请求的日志级别，并且避免多线程情况下日志错乱的问题

    使用方式

    ```java
    FullLoggingInterceptor fullLoggingInterceptor = new FullLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            Logger.d(message);
        }
    });
    fullLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(fullLoggingInterceptor)
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .addCallAdapterFactory(CallAdapterFactory.INSTANCE)
            .addCallAdapterFactory(DownloadCallAdapterFactory.INSTANCE)
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
        Call<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```

    效果图

    ![日志](https://github.com/xchengDroid/retrofit-helper/blob/master/screenshots/logstyle.png)

    

    采用代理的方式拦截 HttpLoggingInterceptor的处理方式

    ```java
    /**
     * 创建时间：2019/9/25
     * 编写人： chengxin
     * 功能描述：打印完整的日志，防止多线程情况下导致的日志分散错乱的问题
     */
    public final class FullLoggingInterceptor implements Interceptor {
        private static final int JSON_INDENT = 2;
        private static final String LOG_LEVEL = "LogLevel";
        private final Logger logger;
        private volatile Level level = Level.NONE;
    
        public FullLoggingInterceptor() {
            this(Logger.DEFAULT);
        }
    
        public FullLoggingInterceptor(Logger logger) {
            this.logger = logger;
        }
    
        /**
         * Change the level at which this interceptor logs.
         */
        public FullLoggingInterceptor setLevel(Level level) {
            if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
            this.level = level;
            return this;
        }
    
        public Level getLevel() {
            return level;
        }
    
        @Override
        public Response intercept(Chain chain) throws IOException {
            final StringBuilder builder = new StringBuilder();
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new Logger() {
                @Override
                public void log(String message) {
                    append(builder, message);
                }
            });
            //可以单独为某个请求设置日志的级别，避免全局设置的局限性
            httpLoggingInterceptor.setLevel(findLevel(chain.request()));
            Response response = httpLoggingInterceptor.intercept(chain);
            logger.log(builder.toString());
            return response;
        }
    
        @NonNull
        private Level findLevel(Request request) {
            //可以单独为某个请求设置日志的级别，避免全局设置的局限性
            String logLevel = request.header(LOG_LEVEL);
            if (logLevel != null) {
                if (logLevel.equalsIgnoreCase("NONE")) {
                    return Level.NONE;
                } else if (logLevel.equalsIgnoreCase("BASIC")) {
                    return Level.BASIC;
                } else if (logLevel.equalsIgnoreCase("HEADERS")) {
                    return Level.HEADERS;
                } else if (logLevel.equalsIgnoreCase("BODY")) {
                    return Level.BODY;
                }
            }
            return level;
        }
    
        private static void append(StringBuilder builder, String message) {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            try {
                // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
                if (message.startsWith("{") && message.endsWith("}")) {
                    JSONObject jsonObject = new JSONObject(message);
                    message = jsonObject.toString(JSON_INDENT);
                } else if (message.startsWith("[") && message.endsWith("]")) {
                    JSONArray jsonArray = new JSONArray(message);
                    message = jsonArray.toString(JSON_INDENT);
                }
            } catch (JSONException ignored) {
            }
            builder.append(message).append('\n');
        }
    }
    ```

    

- #### 3.注意事项

  - 4.1 构建retrofit是需要CallAdapterFactory实例，否则无法处理返回为Call的服务接口

  - 4.2 `Callback`的回调函数均在主线程执行，如果Call绑定了生命周期触发了`cancel()`方法

    UI回调方法均不会执行，如果要监听那些请求被取消了，可以设置`RetrofitFactory.LISTENER`属性，其为一个全局的监听器`OnEventListener`。
    
    

- #### 4.下载

  ```groovy
  dependencies {
       implementation 'com.xcheng:retrofit-helper:1.6.0'
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

