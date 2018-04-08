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

import android.support.annotation.Nullable;

import static com.xcheng.retrofit.Utils.checkNotNull;

/**
 * An HTTP Result. like Retrofit
 */
public final class Result<T> {

    /**
     * @param body 请求成功返回的body
     * @throws NullPointerException if body==null
     */
    public static <T> Result<T> success(T body) {
        checkNotNull(body, "body==null");
        return new Result<>(body, null);
    }

    /**
     * @param error 请求失败返回的错误信息
     * @throws NullPointerException if error==null
     */
    public static <T> Result<T> error(HttpError error) {
        checkNotNull(error, "error==null");
        return new Result<>(null, error);
    }

    @Nullable
    private final HttpError error;
    @Nullable
    private final T body;

    private Result(@Nullable T body, @Nullable HttpError error) {
        this.error = error;
        this.body = body;
    }

    @Nullable
    public HttpError error() {
        return error;
    }

    @Nullable
    public T body() {
        return body;
    }

    /**
     * 判断http请求是否成功返回了body
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return body != null;
    }

}
