package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface LifecycleEvent<R> {

    R bindUntilEvent(@NonNull LifecycleProvider provider, @NonNull Lifecycle.Event event);

    R bindUntilDestroy(@NonNull LifecycleProvider provider);

    void onEvent(@Nullable Lifecycle.Event event);

}
