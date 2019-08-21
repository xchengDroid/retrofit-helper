package com.xcheng.retrofit;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;

/**
 * 统一分发Activity和 Fragment的生命周期时间.
 */
public interface LifecycleProvider {
    /**
     * Adds an observer to the list. The observer cannot be null and it must not already
     * be registered.
     *
     * @param observer the observer to register
     * @throws IllegalArgumentException the observer is null
     */
    void observe(Observer observer);

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     *
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is null
     */
    void removeObserver(Observer observer);

    /**
     * A simple callback that can receive from {@link Lifecycle.Event}.
     */
    interface Observer {
        /**
         * <p>
         * The action may be called concurrently from multiple
         * threads; the action must be thread safe,like{@code Observable#doOnDispose(Action)}.
         * <p>
         * you can invoke with {@link Lifecycle.Event#ON_ANY} to dispose from outside immediately.
         * <p>
         * Called when the event is changed.
         *
         * @param event The new event
         */
        void onChanged(@NonNull Lifecycle.Event event);
    }
}
