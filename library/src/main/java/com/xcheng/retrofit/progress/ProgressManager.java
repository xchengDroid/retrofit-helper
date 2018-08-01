package com.xcheng.retrofit.progress;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.GuardedBy;

import com.xcheng.retrofit.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 创建时间：2018/7/31
 * 编写人： chengxin
 * 功能描述：管理进度监听类
 */
public class ProgressManager {

    private volatile static ProgressManager instance;

    @GuardedBy("listeners")
    private final ArrayList<ProgressListener> listeners =
            new ArrayList<>();
    /**
     * 回调函数是否在主线程,默认为true
     */
    private volatile boolean onMainThread;
    private final Handler mainHandler;

    private ProgressManager() {
        mainHandler = new Handler(Looper.getMainLooper());
        onMainThread = true;
    }

    public static ProgressManager getInstance() {
        if (instance == null) {
            synchronized (ProgressManager.class) {
                if (instance == null) {
                    instance = new ProgressManager();
                }
            }
        }
        return instance;
    }

    public void setOnMainThread(boolean onMainThread) {
        this.onMainThread = onMainThread;
    }

    public boolean isOnMainThread() {
        return onMainThread;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public void registerListener(ProgressListener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                throw new IllegalStateException("ProgressListener " + listener + " is already registered.");
            }
            listeners.add(listener);
        }
    }

    public void unregisterListener(ProgressListener listener) {
        Utils.checkNotNull(listener, "listener==null");
        synchronized (listeners) {
            int index = listeners.indexOf(listener);
            if (index == -1) {
                throw new IllegalStateException("ProgressListener " + listener + " was not registered.");
            }
            listeners.remove(index);
        }
    }

    public void unregisterListeners(String tag) {
        Utils.checkNotNull(tag, "tag==null");
        synchronized (listeners) {
            for (int index = 0; index < listeners.size(); index++) {
                ProgressListener listener = listeners.get(index);
                if (listener.tag.equals(tag)) {
                    listeners.remove(index);
                    index--;
                }
            }
        }
    }


    /**
     * @param tag      对应的tag
     * @param download true代表下载，false代表上传
     * @return 获取对应的Listener, 如果不存在返回size==0的ArrayList
     */
    public List<ProgressListener> getListeners(String tag, boolean download) {
        Utils.checkNotNull(tag, "tag==null");
        List<ProgressListener> listeners = new ArrayList<>();
        synchronized (this.listeners) {
            for (int index = 0; index < this.listeners.size(); index++) {
                ProgressListener listener = this.listeners.get(index);
                if (listener.tag.equals(tag)
                        && listener.download == download) {
                    listeners.add(listener);
                }
            }
        }
        return listeners;
    }

    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    /**
     * 标记Header的key ,如果header上有此键值对，尝试监听进度
     */
    static final String KEY_HTTP_PROGRESS = "HttpProgress";

    public static Interceptor INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String tag = request.header(KEY_HTTP_PROGRESS);
            //先判断是否有进度需求
            if (tag == null)
                return chain.proceed(request);

            RequestBody requestBody = request.body();
            //判断是否有上传需求
            if (requestBody != null) {
                List<ProgressListener> upListeners = getInstance().getListeners(tag, false);
                if (upListeners.size() != 0) {
                    Request.Builder builder = request.newBuilder();
                    RequestBody newRequestBody = new ProgressRequestBody(requestBody, upListeners);
                    request = builder.method(request.method(), newRequestBody).build();
                }
            }
            Response response = chain.proceed(request);
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                List<ProgressListener> downListeners = getInstance().getListeners(tag, true);
                Response.Builder builder = response.newBuilder();
                ResponseBody newResponseBody = new ProgressResponseBody(responseBody, downListeners);
                response = builder.body(newResponseBody).build();
            }
            return response;
        }
    };
}
