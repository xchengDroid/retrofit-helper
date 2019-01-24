#### EasyOkHttp 

------

Retrofit相信很多android开发者都在使用！很多时候我们根据需要为其在封装一层实现。能够更好更简洁的实现我们的业务代码，我们先列一下retrofit使用过程中的一些痛点

1. 取消请求不方便，必须持有发起请求时的Call对象

2. 不能动态修改baseUrl

3. 不能监听下载进度

4. 回调函数 public void onResponse(Call<T> call, final Response<T> response) 我们还需要再次解析Response<T> ,更具是否成功在做业务处理，但是这时候很多代码都是重复判断代码，显得冗余。

5. 没有独立的回调接口监听请求发起 、请求结束、请求取消


我们关心的是请求的结果，比如登录 我们只想要拿到登录信息LoginInfo，或者加载列表的时候我们只需要拿到列表对象List<Item>,请求失败的时候我们很多时候只需要拿到错误信息，弹出给用户同事即可

针对以上问题 ，故做了二次封装。[github EasyOkHttp](https://github.com/xchengDroid/EasyOkHttp) 基于Retrofit的二次封装。解决以上问题

##### Download

> 最新版本依赖  implementation 'com.xcheng:easyokhttp:2.6.2'

**1、发起请求**

全局初始化

```
 Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://wanandroid.com/")
                .callFactory(new OkHttpClient.Builder()
                        .addNetworkInterceptor(httpLoggingInterceptor)
                        .build())
                //必须添加此adapter 用于构建处理回调
                .addCallAdapterFactory(ExecutorCallAdapterFactory.INSTANCE)
                //添加自定义json解析器 
                .addConverterFactory(GsonConverterFactory.create())
                .build();
  RetrofitManager.init(retrofit);
```

定义Service接口

```
@FormUrlEncoded
@POST("user/login")
Call2<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
```

发起请求

```
RetrofitManager.create(ApiService.class)
        .getLogin("xxxxxx", "123456")
        .enqueue(yourTag, new AnimCallback<LoginInfo>(this) {
            @Override
            public void onError(Call2<LoginInfo> call2, HttpError error) {
                Toast.makeText(MainActivity.this, error.msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Call2<LoginInfo> call2, LoginInfo response) {
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            }
        });
```



**2、修改url** 

很多第三方的是通过拦截器实现切换baseurl ，个人认为这样存在一定的安全问题，破坏了retrofit整体性

在EasyOkhttp中实现的方式比较简单和稳定

```
 //先销毁之前的实例
 RetrofitManager.destroy(true);
 //重新设置全局的retrofit实例
 RetrofitManager.init(youRetrofit);
 
 //添加其他的retrofit
 RetrofitManager.put("otherTag",otherRetrofit);
 Retrofit otherRetrofit=RetrofitManager.get("otherTag");
 
```

**3、下载监听进度**

通过添加ProgressInterceptor 实现

```
//构建可以监听进度的client
OkHttpClient client = new OkHttpClient().newBuilder()
        .addNetworkInterceptor(new ProgressInterceptor(new ProgressListener() {
            @Override
            public void onUpload(Request request, long progress, long contentLength, boolean done) {
            }
            @Override
            public void onDownload(Request request, long progress, long contentLength, boolean done) {

            }
        })).build();

//构建可以下载文件的client
Retrofit retrofit = RetrofitManager.retrofit()
        .newBuilder()
        .callFactory(client)
        .addConverterFactory(new FileConverterFactory(filePath))
        .build();
```

实现效果如下

![image-20181115174409086](/Users/chengxin/Library/Application Support/typora-user-images/image-20181115174409086.png)



**4、简洁高效的回调接口**

```
@UiThread
public abstract class Callback2<T> {
    @NonNull
    public Result<T> parseResponse(Call2<T> call2, Response<T> response) {
        return xxx;
    }
    @NonNull
    public HttpError parseThrowable(Call2<T> call2, Throwable t) {
        return xxx;
    }
    public void onStart(Call2<T> call2) {}

    public abstract void onError(Call2<T> call2, HttpError error);

    public abstract void onSuccess(Call2<T> call2, T response);

    public void onCompleted(Call2<T> call2) {
    }
    public void onCancel(Call2<T> call2, @Nullable Throwable failureThrowable, boolean fromFrame) {}
    
    public void onThrowable(Call2<T> call2, Throwable t) {
    }
}
```



> 监听请求开始 ，可以显示loading等正在加载的页面

```
public void onStart(Call2<T> call2) {}
```

> 将Retrofit onResponse方法传入的response解析成你想要的结果包装成Result<T>对象，返回的 Result类中可存放 body 和error

```
public Result<T> parseResponse(Call2<T> call2, Response<T> response) {
        return xxx;
    }
```

> 将Retrofit onFailure方法传入的Throwable 异常包装成你想要的HttpError对象，返回的HttpError类中可存放 msg 和body [你想要传到前端解析详细信息，如session过期啊 未登录等]
>

```
public HttpError parseThrowable(Call2<T> call2, Throwable t) {
        return xxx;
}
```

> 成功时返回你想要的结果
>

```
public abstract void onSuccess(Call2<T> call2, T response);
```

> 失败时返回你想要的错误信息

```
public abstract void onError(Call2<T> call2, HttpError error);
```

> 监听正常请求结束 ，可以结束loading等

```
public void onCompleted(Call2<T> call2) {}
```

> 请求被取消时回调

```
 public void onCancel(Call2<T> call2, @Nullable Throwable failureThrowable, boolean fromFrame) {}
```

> 以上任意回调函数发生奔溃抛出Throwable，将会调用此函数，避免线上奔溃异常退出

```
public void onThrowable(Call2<T> call2, Throwable t) {}
```



##### 5、取消请求

在Activity#onDestroy函数或者其他任意地方传入调用 Call2#enqueue(@Nullable Object tag, Callback2<T> callback2) 时的tag即可

```
CallManager.getInstance().cancel(yourTag);
```

欢迎突出疑问和建议！



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