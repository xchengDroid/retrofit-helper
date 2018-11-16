Retrofit相信很多android开发者都在使用！很多时候我们根据需要为其在封装一层实现。能够更好更简洁的实现我们的业务代码，我们先列一下retrofit使用过程中的一些痛点

1、取消请求不方便，必须持有发起请求时的Call对象

2、不能动态修改baseUrl

3、不能监听下载进度

4、回调函数 public void onResponse(Call<T> call, final Response<T> response) 我们还需要再次解析Response<T> ,更具是否成功在做业务处理，但是这时候很多代码都是重复判断代码，显得冗余。

5、没有独立的回调接口监听 ***请求发起 、请求结束、请求取消***

#### 我们关心的是请求的结果，比如登录 我们只想要拿到登录信息LoginInfo，或者加载列表的时候我们只需要拿到列表对象List<Item>,请求失败的时候我们很多时候只需要拿到错误信息，弹出给用户同事即可

针对以上问题 ，故做了二次封装。[github EasyOkHttp](https://github.com/xchengDroid/EasyOkHttp) 基于Retrofit的二次封装。解决以上问题

*最新的依赖为 `implementation 'com.xcheng:easyokhttp:2.5.0'`*

**1、发起请求**

重新定义了Call 接口 ，命名为Call2

```
/**
* 创建时间：2018/4/8
* 编写人： chengxin
* 功能描述：添加重载方法{@link Call2#enqueue(Object, Callback2)}方法
*/
public interface Call2<T> extends retrofit2.Call<T> {
/**
* @param tag       请求的tag,用于取消请求使用
* @param callback2 请求的回调
*/
void enqueue(@Nullable Object tag, Callback2<T> callback2);

@Override
Call2<T> clone();
}
```

新增 了

`void enqueue(@Nullable Object tag, Callback2<T> callback2);`

传入 tag参数用于控制取消请求

调用 `CallManager.getInstance().cancel(tag;` 取消某个请求

在 onDestory 方法中调用即可

**2、修改url** 

很多第三方的是通过拦截器实现切换baseurl ，个人认为这样存在一定的安全问题，破坏了retrofit整体性

在easyokhttp中实现的方式比较简单和稳定

```
package com.xcheng.retrofit;

import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

/**
* 创建时间：2018/4/3
* 编写人： chengxin
* 功能描述：管理全局的Retrofit实例
*/
public final class RetrofitManager {

private static final String TAG = RetrofitManager.class.getSimpleName();

private static final String LOG_INIT_RETROFIT = "Initialize RetrofitManager with retrofit success";
private static final String WARNING_RE_INIT_RETROFIT = "Try to initialize RetrofitManager which had already been initialized before";
private static final String ERROR_NOT_INIT = "RetrofitManager must be init with retrofit before using";
/**
* 全局的retrofit对象
*/
private static volatile Retrofit sRetrofit;
/**
* 缓存不同配置的retrofit集合，如url ,converter等
*/
private static final Map<String, Retrofit> sRetrofitsCache = new ConcurrentHashMap<>(2);

private RetrofitManager() {
}

/**
* 初始化全局的Retrofit对象,like Charset#bugLevel,HttpLoggingInterceptor#level,
* AsyncTask#mStatus,facebook->stetho->LogRedirector#sLogger
*
* @param retrofit 全局的Retrofit对象
*/
public static void init(Retrofit retrofit) {
Utils.checkNotNull(retrofit, "retrofit==null");
if (sRetrofit == null) {
Log.d(TAG, LOG_INIT_RETROFIT);
sRetrofit = retrofit;
} else {
Log.e(TAG, WARNING_RE_INIT_RETROFIT);
}
}

/**
* like {@link retrofit2.OkHttpCall#cancel()}
*
* @return true if has init
*/
public static boolean isInited() {
//synchronized获得锁时会清空工作内存，从主内存重新获取最新数据
//同步判断Retrofit是否已经初始化，防止此时正在同步块初始化
return sRetrofit != null;
}

public static void destroy(boolean isAll) {
sRetrofit = null;
if (isAll) {
sRetrofitsCache.clear();
}
}

public static <T> T create(Class<T> service) {
return retrofit().create(service);
}

public static Retrofit retrofit() {
final Retrofit retrofit = sRetrofit;
if (retrofit == null) {
throw new IllegalStateException(ERROR_NOT_INIT);
}
return retrofit;
}

/**
* 全局保存不同配置的Retrofit,如不同的baseUrl等
*
* @param tag      标记key
* @param retrofit 对应的retrofit对象
*/
public static void put(String tag, Retrofit retrofit) {
sRetrofitsCache.put(tag, retrofit);
}

public static Retrofit get(String tag) {
return sRetrofitsCache.get(tag);
}

public static void remove(String tag) {
sRetrofitsCache.remove(tag);
}
}
```

在使用前初始化即可 `RetrofitManager.init(Retrofit)` 初始化全局的retrofit对象， 可用通过调用`RetrofitManager.destory(boolean)` 重新初始化。如果有多个baseUrl 调用`RetrofitManager. put(String, Retrofit)`

**3、下载监听进度**

通过添加ProgressInterceptor 实现

```
package com.xcheng.retrofit.progress;

import com.xcheng.retrofit.Utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
* 创建时间：2018/8/2
* 编写人： chengxin
* 功能描述：上传或下载进度监听拦截器
*/
public class ProgressInterceptor implements Interceptor {

private final ProgressListener mProgressListener;

public ProgressInterceptor(ProgressListener progressListener) {
Utils.checkNotNull(progressListener, "progressListener==null");
this.mProgressListener = progressListener;
}

@Override
public Response intercept(Chain chain) throws IOException {
Request request = chain.request();
RequestBody requestBody = request.body();
//判断是否有上传需求
if (requestBody != null && requestBody.contentLength() > 0) {
Request.Builder builder = request.newBuilder();
RequestBody newRequestBody = new ProgressRequestBody(requestBody, mProgressListener, request);
request = builder.method(request.method(), newRequestBody).build();
}

Response response = chain.proceed(request);
ResponseBody responseBody = response.body();
if (responseBody != null && responseBody.contentLength() > 0) {
Response.Builder builder = response.newBuilder();
ResponseBody newResponseBody = new ProgressResponseBody(responseBody, mProgressListener, request);
response = builder.body(newResponseBody).build();
}
return response;
}
}
```

实现效果如下

![image-20181115174409086](/Users/chengxin/Library/Application Support/typora-user-images/image-20181115174409086.png)



**4、简洁高效的回调接口**

```
package com.xcheng.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Response;

/**
* if {@link Call#cancel()}called {@link #onStart(Call2)}、 {@link #onSuccess(Call2, Object)}、
* {@link #onError(Call2, HttpError)}、 {@link #onCompleted(Call2)} will not be called
*
* @param <T> Successful response body type.
*/
@UiThread
public abstract class Callback2<T> {

@NonNull
public Result<T> parseResponse(Call2<T> call2, Response<T> response) {
T body = response.body();
if (response.isSuccessful()) {
if (body != null) {
return Result.success(body);
} else {
return Result.error(new HttpError("暂无数据", response));
}
}

final String msg;
switch (response.code()) {
case 400:
msg = "参数错误";
break;
case 401:
msg = "身份未授权";
break;
case 403:
msg = "禁止访问";
break;
case 404:
msg = "地址未找到";
break;
default:
msg = "服务异常";
}
return Result.error(new HttpError(msg, response));
}

/**
* 统一解析Throwable对象转换为HttpError对象。如果为HttpError，
* 则为{@link retrofit2.Converter#convert(Object)}内抛出的异常
*
* @param call2 call
* @param t     Throwable
* @return HttpError result
*/
@NonNull
public HttpError parseThrowable(Call2<T> call2, Throwable t) {
if (t instanceof HttpError) {
//用于convert函数直接抛出异常接收
return (HttpError) t;
} else if (t instanceof UnknownHostException) {
return new HttpError("网络异常", t);
} else if (t instanceof ConnectException) {
return new HttpError("网络异常", t);
} else if (t instanceof SocketException) {
return new HttpError("服务异常", t);
} else if (t instanceof SocketTimeoutException) {
return new HttpError("响应超时", t);
} else {
return new HttpError("请求失败", t);
}
}

public void onStart(Call2<T> call2) {
}

public void onCancel(Call2<T> call2) {
}

public abstract void onError(Call2<T> call2, HttpError error);

public abstract void onSuccess(Call2<T> call2, T response);

/**
* 请求回调全部完成时执行
*
* @param call2 Call
*/
public void onCompleted(Call2<T> call2) {
}
}
```

能够监听 请求开始 `onStart(Call2<T> call2)`请求结束  `onCompleted(Call2<T> call2)`请求取消 `onCancel(Call2<T> call2)` ，可用重写`parseThrowable(Call2<T> call2, Throwable t)` 处理其他异常如

```
@NonNull
@Override
public HttpError parseThrowable(Call2<T> call2, Throwable t) {
HttpError filterError;
if (t instanceof JsonSyntaxException) {
filterError = new HttpError("解析异常", t);
} else {
filterError = super.parseThrowable(call2, t);
}
return filterError;
}
```



核心的解析接口如下，在通用的接口返回一般是这样

```
/**
* 普通的结果提示 ，code=0代表成功
* Created by chengxin on 2017/9/26.
*/
public class BaseResult<T> {
private int code = -1;
private String msg;
private T data;

public T getData() {
return data;
}

public int getCode() {
return code;
}

public String getMsg() {
return msg;
}

public boolean isSuccess() {
return code == 0;
}
}
```

其实我们关系的只有成功时候的data 或者失败时的msg 和异常信息Exception等

故我们将返回的解析简化 如下：

```
package com.simple.converter;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.simple.okhttp.Tip;
import com.xcheng.retrofit.HttpError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
* 创建时间：2018/4/3
* 编写人： chengxin
* 功能描述：json解析相关
*/
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
private final Gson gson;
private final Type type;

GsonResponseBodyConverter(Gson gson, Type type) {
this.type = type;
this.gson = gson;
}

@SuppressWarnings("unchecked")
@Override
public T convert(@NonNull ResponseBody value) throws IOException {
String cacheStr = value.string();
try {
JSONObject jsonObject = new JSONObject(cacheStr);
final int code = jsonObject.getInt("errorCode");
final String msg = jsonObject.getString("errorMsg");
Tip tip = new Tip(code, msg);
if (code != 0) {
throw new HttpError(msg, tip);
}
Class<?> rawType = $Gson$Types.getRawType(type);
if (Tip.class == rawType) {
return (T) tip;
}
Object data = jsonObject.get("data");
if (data == JSONObject.NULL) {
//in case
throw new HttpError("暂无数据", tip);
}
//如果是String 直接返回
if (String.class == rawType) {
return (T) data.toString();
}
//data 为Boolean 如{"msg": "手机号格式错误","code": 0,"data": false}
if (Boolean.class == rawType && data instanceof Boolean) {
return (T) data;
}
//data 为Integer  如{"msg": "手机号格式错误","code": 0,"data": 12}
if (Integer.class == rawType && data instanceof Integer) {
return (T) data;
}
T t = gson.fromJson(data.toString(), type);
if (t != null) {
//防止线上接口修改导致反序列化失败奔溃
return t;
}
throw new HttpError("数据异常", tip);
} catch (JSONException e) {
throw new HttpError("解析异常", cacheStr);
}
}
}
```

如果不是我们想要的正常成功返回，就抛出Exception！封装成HttpError

```
package com.xcheng.retrofit;

import android.support.annotation.Nullable;

/**
* 通用的错误信息，一般请求是失败只需要弹出一些错误信息即可,like{@link retrofit2.HttpException}
* Created by chengxin on 2017/6/22.
*/
public final class HttpError extends RuntimeException {
private static final long serialVersionUID = -134024482758434333L;
/**
* 展示在前端的错误描述信息
*/
public String msg;

/**
* <p>
* 请求失败保存失败信息,for example:
* <li>BusiModel: {code:xxx,msg:xxx} 业务错误信息</li>
* <li>original json:  原始的json</li>
* <li>{@link retrofit2.Response}:错误响应体->Response<?></li>
* <li>Throwable: 抛出的异常信息</li>
* </p>
*/
@Nullable
public final transient Object body;

public HttpError(String msg) {
this(msg, null);
}

public HttpError(String msg, @Nullable Object body) {
super(msg);
if (body instanceof Throwable) {
initCause((Throwable) body);
}
//FastPrintWriter#print(String str)
this.msg = msg != null ? msg : "null";
this.body = body;
}

/**
* 保证和msg一致
*/
@Override
public String getMessage() {
return msg;
}

@Override
public String toString() {
return "HttpError {msg="
+ msg
+ ", body="
+ body
+ '}';
}
}
```

在回调失败函数中 `onError(Call2<T> call2, HttpError error)` 处理即可，简洁明了！

欢迎突出疑问和建议！

> [GitHub]: https://github.com/xchengDroid/EasyOkHttp
