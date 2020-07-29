/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xcheng.retrofit;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.logging.HttpLoggingInterceptor.Logger;

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
    public void setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
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
