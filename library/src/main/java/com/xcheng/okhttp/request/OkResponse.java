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
package com.xcheng.okhttp.request;

import android.support.annotation.Nullable;

import com.xcheng.okhttp.error.EasyError;
import com.xcheng.okhttp.util.EasyPreconditions;

/**
 * An HTTP response.
 */
public final class OkResponse<T> {

    /**
     * @param body 请求成功返回的body
     * @throws NullPointerException if body==null
     */
    public static <T> OkResponse<T> success(T body) {
        EasyPreconditions.checkNotNull(body, "body==null");
        return new OkResponse<>(body, null);
    }

    /**
     * @param easyError 请求失败返回的错误信息
     * @throws NullPointerException if easyError==null
     */
    public static <T> OkResponse<T> error(EasyError easyError) {
        EasyPreconditions.checkNotNull(easyError, "easyError==null");
        return new OkResponse<>(null, easyError);
    }

    @Nullable
    private final EasyError easyError;
    @Nullable
    private final T body;

    private OkResponse(@Nullable T body, @Nullable EasyError easyError) {
        this.easyError = easyError;
        this.body = body;
    }

    @Nullable
    public EasyError getError() {
        return easyError;
    }

    @Nullable
    public T getBody() {
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
