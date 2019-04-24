

### retrofit-helper 

> Retrofit是很多android开发者都在使用的Http请求库！他负责网络请求接口的封装,底层实现是OkHttp,它的一个特点是包含了特别多注解，方便简化你的代码量,CallAdapter.Factory 和Converter.Factory可以很灵活的扩展你的请求，但是我们在使用的时候还是需要封装一层便于我们使用，retrofit-helper的出现就是再简化你的请求。



##### retrofit-helper  拓张了那些功能呢

- 回调函数中需要我们再次从Response<T>中处理数据，判断是否确定成功

-  需要手动实现下载监听

- 没有请求start和completed的监听，方便开启动画和结束动画

- onDestory中取消请求需要引用Call对象，不灵活

- 全局设置多个retrofit对象

  

##### retrofit-helper解析

##### 1. RetrofitFactory 全局管理多个retrofit实例

```
public final class RetrofitFactory {
    /**
     * 缓存不同配置的retrofit集合，如不同的url ,converter等
     */
    public static final Map<String, Retrofit> OTHERS = new ConcurrentHashMap<>(2);
    /**
     * 全局的Retrofit对象
     */
    public static volatile Retrofit DEFAULT;

    private RetrofitFactory() {
    }

    public static <T> T create(Class<T> service) {
        //确保多线程的情况下retrofit不为空或者被改变了 Charset#atBugLevel(String bl)
        Retrofit retrofit = DEFAULT;
        //此时由于多线程导致值被修改了不管
        if (retrofit == null) {
            throw new IllegalStateException("DEFAULT == null");
        }
        return retrofit.create(service);
    }

    /**
     * @param name 获取 OTHERS 中指定名字的retrofit
     */
    public static <T> T create(String name, Class<T> service) {
        Utils.checkNotNull(name, "name == null");
        Retrofit retrofit = OTHERS.get(name);
        if (retrofit == null) {
            throw new IllegalStateException(String.format("retrofit named with \'%s\' was not found , have you put it in OTHERS ?", name));
        }
        return retrofit.create(service);
    }
}
```

<u></u>



##### 2. Call2接口重载enquene方法，传入tag用以标记当前请求

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



##### 3. 添加Callback2回调接口，监听请求开始和结束，并二次处理htttp返回的数据，返回给前端需要的数据

```
/**
 * if {@link Call#cancel()}called, {@link #onStart(Call2)}、{@link #parseResponse(Call2, Response)}
 * 、{@link #parseThrowable(Call2, Throwable)}、{@link #onSuccess(Call2, Object)}、
 * {@link #onError(Call2, HttpError)}will not be called
 *
 * @param <T> Successful response body type.
 */
@UiThread
public abstract class Callback2<T> {

    public abstract void onStart(Call2<T> call2);

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

    public abstract void onError(Call2<T> call2, HttpError error);

    public abstract void onSuccess(Call2<T> call2, T response);


    /**
     * @param t        请求失败的错误信息
     * @param canceled 请求是否被取消了
     */
    public abstract void onCompleted(Call2<T> call2, @Nullable Throwable t, boolean canceled);
}
```

##### 4. Call2的实现类，代理处理http请求,回调请求结果

```java
/**
 * just for android post UI thread
 */
public final class ExecutorCallAdapterFactory extends CallAdapter.Factory {

    public static final CallAdapter.Factory INSTANCE = new ExecutorCallAdapterFactory();

    private ExecutorCallAdapterFactory() {
    }

    /**
     * Extract the raw class type from {@code type}. For example, the type representing
     * {@code List<? extends Runnable>} returns {@code List.class}.
     */
    public static Class<?> getRawType(Type type) {
        return CallAdapter.Factory.getRawType(type);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call2.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Call return type must be parameterized as Call2<Foo> or Call2<? extends Foo>");
        }
        final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);

        final Executor callbackExecutor = retrofit.callbackExecutor();
        if (callbackExecutor == null) throw new AssertionError();

        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return new ExecutorCallbackCall2<>(callbackExecutor, call);
            }
        };
    }

    static final class ExecutorCallbackCall2<T> implements Call2<T> {
        private final Executor callbackExecutor;
        private final Call<T> delegate;

        /**
         * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
         * in which case callbacks should be made synchronously on the background thread.
         */
        ExecutorCallbackCall2(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            throw new UnsupportedOperationException("please call enqueue(Object tag, Callback2<T> callback2)");
        }

        @Override
        public void enqueue(@Nullable Object tag, final Callback2<T> callback2) {
            Utils.checkNotNull(callback2, "callback2==null");
            CallManager.getInstance().add(this, tag != null ? tag : "NO_TAG");
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (!isCanceled()) {
                        callback2.onStart(ExecutorCallbackCall2.this);
                    }
                }
            });

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callResult(callback2, response, null);
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callResult(callback2, null, t);
                        }
                    });
                }
            });
        }

        @UiThread
        private void callResult(Callback2<T> callback2, @Nullable Response<T> response, @Nullable Throwable failureThrowable) {
            try {
                if (!isCanceled()) {
                    //1、获取解析结果
                    Result<T> result;
                    if (response != null) {
                        result = callback2.parseResponse(this, response);
                        Utils.checkNotNull(result, "result==null");
                    } else {
                        Utils.checkNotNull(failureThrowable, "failureThrowable==null");
                        HttpError error = callback2.parseThrowable(this, failureThrowable);
                        result = Result.error(error);
                    }
                    //2、回调成功失败
                    if (result.isSuccess()) {
                        callback2.onSuccess(this, result.body());
                    } else {
                        callback2.onError(this, result.error());
                    }
                }
                callback2.onCompleted(this, failureThrowable, isCanceled());
            } finally {
                CallManager.getInstance().remove(this);
            }
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call2<T> clone() {
            return new ExecutorCallbackCall2<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }
}
```

##### 5. CallManager 全局管理Call对象

```
/**
 * 创建时间：2018/5/31
 * 编写人： chengxin
 * 功能描述：全局管理Call请求管理,just like {@link okhttp3.Dispatcher}
 */
public final class CallManager implements ActionManager<Call<?>> {
    @GuardedBy("this")
    private final List<CallTag> callTags = new ArrayList<>(4);
    private volatile static CallManager instance;

    private CallManager() {
    }

    public static CallManager getInstance() {
        if (instance == null) {
            synchronized (CallManager.class) {
                if (instance == null) {
                    instance = new CallManager();
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized void add(Call<?> call, Object tag) {
        Utils.checkState(!contains(call), "Call<?>  " + call + " is already added.");
        callTags.add(new CallTag(call, tag));
    }

    /**
     * 当call结束时移除
     *
     * @param call Retrofit Call
     */
    @Override
    public synchronized void remove(Call<?> call) {
        if (callTags.isEmpty())
            return;
        for (int index = 0; index < callTags.size(); index++) {
            if (call == callTags.get(index).call) {
                //like okhttp3.Headers#removeAll(String name)
                //remove(int index) 方法优于 remove(Object o)，无需再次遍历
                callTags.remove(index);
                break;
            }
        }
    }

    /**
     * 取消并移除对应tag的call，确保Call被取消后不再被引用，
     * 结合{@link #remove(Call)}方法双保险
     *
     * @param tag call对应的tag
     */
    @Override
    public synchronized void cancel(final @Nullable Object tag) {
        if (callTags.isEmpty())
            return;
        if (tag != null) {
            for (int index = 0; index < callTags.size(); index++) {
                CallTag callTag = callTags.get(index);
                if (callTag.tag.equals(tag)) {
                    callTag.call.cancel();
                    callTags.remove(index);
                    index--;
                }
            }
        } else {
            for (CallTag callTag : callTags) {
                callTag.call.cancel();
            }
            callTags.clear();
        }
    }

    @Override
    public synchronized boolean contains(Call<?> call) {
        for (CallTag callTag : callTags) {
            if (call == callTag.call) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存call和tag
     */
    final static class CallTag {
        private final Call<?> call;
        private final Object tag;

        CallTag(Call<?> call, Object tag) {
            Utils.checkNotNull(call == null, "call==null");
            Utils.checkNotNull(tag == null, "tag==null");
            this.call = call;
            this.tag = tag;
        }
    }
}
```



#### 以此为例如何实现登录请求

```java
// 1、初始化Retrofit 
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
  RetrofitFactory.DEFAULT = retrofit;

//相关类实现
/**
 * 普通的结果提示 ，code=0代表成功
 * Created by chengxin on 2017/9/26.
 */
public class Tip {
    private int code = -1;
    private String msg;

    public Tip(int code, String msg) {
        this.code = code;
        this.msg = msg;
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

/**
 * 创建时间：2018/4/3
 * 编写人： chengxin
 * 功能描述：json解析相关
 */
final class GsonResponseBodyConverter<T> extends BaseGsonConverter<T> {
    private final Gson gson;

    GsonResponseBodyConverter(Gson gson, Type type) {
        super(type, $Gson$Types.getRawType(type));
        this.gson = gson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(@NonNull ResponseBody value) throws IOException {
        String cacheStr = value.string();
        try {
            JSONObject jsonObject = new JSONObject(cacheStr);
            final int code = jsonObject.getInt("code");
            final String msg = jsonObject.getString("msg");
            Tip tip = new Tip(code, msg);
            if (code != 0) {
                throw new HttpError(msg, tip);
            }
            if (Tip.class == rawType) {
                return (T) tip;
            }
            //这样判断能防止服务端忽略data字段导致jsonObject.get("data")方法奔溃
            //且能判断为null或JSONObject#NULL的情况
            if (jsonObject.isNull("data")) {
                throw new HttpError("数据为空", tip);
            }
            Object data = jsonObject.get("data");
            if (isEmptyJSON(data)) {
                throw new HttpError("暂无数据", tip);
            }
            //data 基础类型 如{"msg": "xxx","code": xxx,"data": true}
            T t = convertBaseType(data, rawType);
            if (t != null) {
                return t;
            }
            t = gson.fromJson(data.toString(), type);
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


//2、添加登录接口
@FormUrlEncoded
@POST("user/login")
Call2<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);

//3、发起登录请求
RetrofitFactory.create(ApiService.class)
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


//4、取消任意请求
CallManager.getInstance().cancel(yourTag);

 
```



#### 如何监听进度?通过添加ProgressInterceptor 实现

```java
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

//构建可以下载文件的client 发起请求即可
Retrofit retrofit = RetrofitManager.DEFAULT
        .newBuilder()
        .callFactory(client)
        .addConverterFactory(new FileConverterFactory(filePath))
        .build();
```



[retrofit-helper]: https://github.com/xchengDroid/retrofit-helper	"retrofit-helper"



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
