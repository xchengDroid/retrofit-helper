### retrofit-helper 

> Retrofit是很多android开发者都在使用的Http请求库！他负责网络请求接口的封装,底层实现是OkHttp,它的一个特点是包含了特别多注解，方便简化你的代码量,CallAdapter.Factory 和Converter.Factory可以很灵活的扩展你的请求。我们在使用的时候还是需要封装一层便于我们使用，retrofit-helper的作用就是再次简化你的请求。



- #### 1. Retrofit-helper扩展了那些功能

  | 描述                                             | 相关类和方法                                                 |
  | ------------------------------------------------ | ------------------------------------------------------------ |
  | 回调函数中直接处理请求结果，无需再次判断是否成功 | `Callback2.onSuccess(Call2<T> call2, T response)`            |
  | 请求开始和结束监听                               | `Callback2.onStart(Call2<T> call2)`  和`Callback2.onCompleted(Call2<T> call2, @Nullable Throwable t, boolean canceled);` |
  | 全局维护多个Retrofit实例                         | `RetrofitFactory.DEFAULT` 和 `RetrofitFactory.OTHERS`        |
  | 统一处理解析结果                                 | `Callback2.parseResponse(Call2<T> call2, Response<T> response)`和   `Callback2.parseThrowable(Call2<T> call2, Throwable t)` |
  | 全局取消某个请求                                 | `CallManager.getInstance().cancel( yourTag )`                |
  | 拦截器监听下载和上传进度                         | `ProgressInterceptor` 、`ProgressListener`                   |
  | 单独指定某个请求的日志级别                       | `HttpLoggingInterceptor`                                     |

- #### 2. 封装逻辑解析

  - 2.1  `RetrofitFactory`全局管理`retrofit`实例

     DEFAULT 静态变量管理默认常用的的retrofit对象，OTHERS 管理其他多个不同配置的retrofit

    ```java
    /**
     * 创建时间：2018/4/3
     * 编写人： chengxin
     * 功能描述：管理全局的Retrofit实例
     */
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
            //确保多线程的情况下retrofit不为空或者被修改了
            Retrofit retrofit = DEFAULT;
            Utils.checkState(retrofit != null, "DEFAULT == null");
            return retrofit.create(service);
        }
    
        /**
         * @param name 获取 OTHERS 中指定名字的retrofit
         */
        public static <T> T create(String name, Class<T> service) {
            Utils.checkNotNull(name, "name == null");
            Retrofit retrofit = OTHERS.get(name);
            Utils.checkState(retrofit != null,
                    String.format("retrofit named with '%s' was not found , have you put it in OTHERS ?", name));
            return retrofit.create(service);
        }
    }
    ```

  - 2.2  `Call2`接口继承`retrofit.Call` 重载 ` enqueue(Callback<T> callback)`方法

    ​	 `enqueue(@Nullable Object tag, Callback2<T> callback2)` 方法传入请求的tag标记此请求，tag标签就是取消请求所需要的

    ```java
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

    

  - 2.3  `Callback2` 统一处理回调

    ​        请求开始、成功处理、失败处理、成功回调、失败回调、请求结束在此统一处理，各方法可以根据业务的不同自行重写，例如：可以重写`parseResponse`方法根据不同的http code做不同的提示描述 或者

    重写`parseThrowable`方法处理各种Throwable

    ```java
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

    

    ​		

  - 2.4  `HttpError` 统一处理异常错误

    ​       HttpError类中有两个成员属性msg 被body，msg是保存错误的描述信息等，body可以保存异常的具体信息或者原始的json等，`onError(Call2<T> call2, HttpError error)`回调方法可以根据body的具体信息做二次处理。

    ```java
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

    

  - 2.5  `ExecutorCallAdapterFactory`返回`Call2`请求适配器

    处理请求接口方法返回为Call2的请求适配器工厂类

    ```java
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
    }
    ```

  - 2.6  `ExecutorCallbackCall2` 继承`Call2`代理`OkHttpCall`处理UI回调

     装饰者模式代理`OkHttpCall`的所有方法，线程调度处理 `Callback2` 的回调方法在主线程执行

    ```java
    final class ExecutorCallbackCall2<T> implements Call2<T> {
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
    ```

    

  - 2.7  `CallManager`统一管理请求，取消请求

    全局保存所有的请求，添加 、删除请求，取消某个某些匹配tag的请求。可以在Activity 或Fragment的销毁方法中调用`CallManager.getInstance().cancel( yourTag )` 

    ```java
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

    

  - 2.8  `ProgressInterceptor` 拦截器监听下载和上传进度

     继承`okhttp3.Interceptor`  ，构造方法中传入`ProgressListener`监听进度

    ```java
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

  - 2.9  `HttpLoggingInterceptor` 可以单独指定某个请求的日志级别

    构造OkhttpClient时添加此拦截器，在请求的服务方法中添加注解

    @Headers("LogLevel:NONE") 或 @Headers("LogLevel:BASIC") 或 @Headers("LogLevel:HEADERS") 或@Headers("LogLevel:BODY")

    ```java
    @FormUrlEncoded
    @Headers("LogLevel:HEADERS")
    @POST("user/login")
    Call2<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```

- #### 3.实战

  - 3.1 初始化全局Retrofit对象

    ```java
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
     
     //可以添加多个，如：
     RetrofitFactory.OTHERS.put("other",otherRetrofit);
     
    ```

  - 3.2 添加请求服务接口

    下面为登录的 post请求

    ```java
    @FormUrlEncoded
    @POST("user/login")
    Call2<LoginInfo> getLogin(@Field("username") String username, @Field("password") String password);
    ```

  - 3.3 添加ILoadingView，用于开启和结束动画

    Activity 或者Fragment 可以继承  ILoadingView接口实现开始和结束动画

    ```java
    public interface ILoadingView {
        /**
         * 显示加载
         */
        void showLoading();
    
        /**
         * 隐藏加载
         */
        void hideLoading();
    
    }
    ```

  

  - 3.4 添加AnimCallback 处理动画

    这里重写`parseThrowable`处理一些`Callback2`中为未处理的异常

    ```java
    public abstract class AnimCallback<T> extends Callback2<T> {
        private ILoadingView mLoadingView;
    
        public AnimCallback(@Nullable ILoadingView loadingView) {
            this.mLoadingView = loadingView;
        }
    
        @Override
        public void onStart(Call2<T> call2) {
            if (mLoadingView != null)
                mLoadingView.showLoading();
        }
    
        @Override
        public void onCompleted(Call2<T> call2, @Nullable Throwable t, boolean canceled) {
            if (canceled)
                return;
            if (mLoadingView != null)
                mLoadingView.hideLoading();
        }
    
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
    }
    ```

  - 3.5 发起请求

    ```java
    RetrofitFactory.create(ApiService.class)
            .getLogin("xxxxx", "123456")
            .enqueue(hashCode(), new AnimCallback<LoginInfo>(this) {
                @Override
                public void onError(Call2<LoginInfo> call2, HttpError error) {
                    //处理失败
                }
    
                @Override
                public void onSuccess(Call2<LoginInfo> call2, LoginInfo response) {
                   //处理成功 如保存登录信息等
                }
            });
            
            
     //在onDestor中取消未结束的请求
       @Override
        protected void onDestroy() {
            super.onDestroy();
            //hashCode() 能保证唯一性，取消当前页面所发起的所有请求，只要
            // enqueue(tag, callback2) 传入的是对应的hashCode() 即可
            CallManager.getInstance().cancel(hashCode());
        }
    ```

- #### 4.注意事项

  - 4.1 构建retrofit是需要ExecutorCallAdapterFactory实例，否则无法处理返回为Call2的服务接口

  - 4.2 `Callback2`的回调函数均在主线程执行，如果调用了`Call2.cancel()`方法，除了`onCompleted()`方法会执行外其他回调方法都不会执行

    

- #### 5.下载

  



