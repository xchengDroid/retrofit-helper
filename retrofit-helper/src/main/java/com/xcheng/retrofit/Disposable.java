package com.xcheng.retrofit;

/**
 * Represents a disposable resource.
 */
public interface Disposable {
    /**
     * Dispose the resource, the operation should be idempotent.
     */
    void dispose();

    /**
     * Returns true if this resource has been disposed.
     *
     * @return true if this resource has been disposed
     */
    boolean isDisposed();
}
