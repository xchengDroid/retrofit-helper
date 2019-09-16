package com.xcheng.retrofit;

import android.support.annotation.FloatRange;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ProgressIncrease {
    @FloatRange(from = 0.0, to = 1.0, fromInclusive = false, toInclusive = false)
    float value();
}