package com.xcheng.retrofit;

import retrofit2.Call;

/**
 * Callback methods are executed using the {@link retrofit2.Retrofit} callback executor. When none is
 * specified, the following defaults are used:
 * <ul>
 * <li>Android: Callbacks are executed on the application's main (UI) thread.</li>
 * <li>JVM: Callbacks are executed on the background thread which performed the request.</li>
 * </ul>
 * <p>
 *
 * @param <T> Successful response body type.
 */
public interface Callback<T> extends retrofit2.Callback<T> {
    /**
     * @param call The {@code Call} that was started
     */
    void onStart(Call<T> call);

    /**
     * @param call The {@code Call} that was completed
     */
    void onCompleted(Call<T> call);
}