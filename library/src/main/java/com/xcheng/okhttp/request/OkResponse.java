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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xcheng.okhttp.error.BaseError;

/**
 * An HTTP response.
 */
public final class OkResponse<T> {


    public static <T> OkResponse<T> success(@NonNull T body) {
        return new OkResponse<>(body, null);
    }

    public static <T> OkResponse<T> error(@NonNull BaseError baseError) {
        return new OkResponse<>(null, baseError);
    }

    @Nullable
    private final BaseError baseError;
    @Nullable
    private final T body;

    private OkResponse(@Nullable T body, @Nullable BaseError baseError) {
        this.baseError = baseError;
        this.body = body;
    }

    @Nullable
    public BaseError getError() {
        return baseError;
    }

    @Nullable
    public T getBody() {
        return body;
    }
}
