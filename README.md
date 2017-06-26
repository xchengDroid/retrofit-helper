EasyOkHttp

easyOkHttp是基于okhttp的工具类库，简化okhttp发起请求的步骤，提供ui层安全的回调接口，简化操作

支持功能

1. http请求构造灵活简洁
2. 回调函数支持监听上传进度，泛型动态生成 google gson库反序列化 json所需的TypeToken,无需通过匿名类new TypeToken<Bean>(){} 方法创建
3. 自定义http解析类灵活复用，不用重复在Callback中重复编写解析代码,自定义灵活的错误信息
4. 安全返回所需的javaBean,保证不会出现线上NullPointException错误。
5. 全局设置http解析器，host 
   

配置EasyOkHttp

初始化EasyOkHttp

    EasyOkHttp.init(new OkHttpClient(), "http://www.weather.com.cn/", StringParse.class, false);

1. 设置OKHttpClient
2. 设置全局的host
3. 设置解析器，可以自定义 继承 ResponseParse<T>即可
4. 设置当 Call.cancel()被调用后 发生IOException(“canceled”) 是否回调Callback

发起http请求获取string

    GetRequest getRequest = EasyOkHttp.get("https://github.com/").build();
    OKHttpCall<String> okCall = new OKHttpCall<>(getRequest);
    okCall.enqueue(new UICallback<String>() {
        @Override
        public void onError(@NonNull BaseError error, int id) {
            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
    
        }
    
        @Override
        public void onSuccess(@NonNull String response, int id) {
            webView.loadData(response, "text/html", "utf-8");
        }
    });

获取到了 github页面的html

获取天气json

     GetRequest getRequest = EasyOkHttp.get("data/cityinfo/101010100.html").responseParse(JsonParse.class).build();
            OKHttpCall<Weather> okCall = new OKHttpCall<>(getRequest);
            okCall.enqueue(new UICallback<Weather>() {
                @Override
                public void onError(@NonNull BaseError error, int id) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
    
                }
    
                @Override
                public void onSuccess(@NonNull Weather response, int id) {
                    Toast.makeText(MainActivity.this, new Gson().toJson(response), Toast.LENGTH_SHORT).show();
                }
            });

此处的url地址即为全局配置的host+"data/cityinfo/101010100.html",返回的天气json 对应生成的Weather对象。

获取Bitmap

    GetRequest getRequest = EasyOkHttp.get("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499010173&di=9599915fd6f9eb51f527cbbf62a84bd6&imgtype=jpg&er=1&src=http%3A%2F%2F4493bz.1985t.com%2Fuploads%2Fallimg%2F160119%2F5-16011Z92519.jpg").responseParse(BitmapParse.class).build();
    OKHttpCall<Bitmap> okCall = new OKHttpCall<>(getRequest);
    okCall.enqueue(new UICallback<Bitmap>() {
        @Override
        public void onError(@NonNull BaseError error, int id) {
            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    
        @Override
        public void onSuccess(@NonNull Bitmap response, int id) {
            imageView.setImageBitmap(response);
        }
    });



自定义解析器,继承ResponseParse,泛型填写代表你需要解析成的对象，

    public class BeanParse<T> extends ResponseParse<T> {
    
        @NonNull
        @Override
        public OkResponse<T> parseNetworkResponse(OkCall<T> okCall, Response response, int id) throws IOException {
            if (response.isSuccessful()) {
                // to do you parse
                // return  OkResponse.success(bean);
            }
            return OkResponse.error(new BaseError(-101/*your defined errorCode*/, "you errorMessage"));
        }
    }
    
      GetRequest getRequest = EasyOkHttp.get("your url").responseParse(BeanParse.class).build();
            OKHttpCall<Bean> okBeanCall = new OKHttpCall<>(getRequest);
            okBeanCall.enqueue(new UICallback<Bean>() {
                @Override
                public void onError(@NonNull BaseError error, int id) {
                }
    
                @Override
                public void onSuccess(@NonNull Bean response, int id) {
                }
            });

后期更新Form表当提交 ，下载进度回调等，设置okhttps

参考资料
[hongyang的okhttpUtils](https://github.com/hongyangAndroid/okhttputils)
![str](https://github.com/xchengDroid/EasyOkHttp/blob/master/screenshots/str2.png)
![json](https://github.com/xchengDroid/EasyOkHttp/blob/master/screenshots/json2.png)
![lyimage2](https://github.com/xchengDroid/EasyOkHttp/blob/master/screenshots/lyimage2.png)





	














