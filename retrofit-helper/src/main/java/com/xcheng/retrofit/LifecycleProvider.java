package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;

/**
 * 统一分发Activity和 Fragment的生命周期时间
 */
@SuppressWarnings("JavadocReference")
public interface LifecycleProvider {
    /**
     * Adds an observer to the list. The observer cannot be null and it must not already
     * be registered.
     *
     * @param observer the observer to register
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException    the observer is already registered
     */
    void observe(Observer observer);

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     *
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException    the observer is not yet registered
     */
    void removeObserver(Observer observer);

    /**
     * A simple callback that can receive from {@link Lifecycle.Event}.
     */
    interface Observer {
        /**
         * Called when the event is changed.
         *
         * @param event The new event
         */
        void onChanged(@NonNull Lifecycle.Event event);
    }
}
