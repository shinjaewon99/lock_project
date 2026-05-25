package com.side.room_project.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimisticRetry {
    int maxRetries() default 100; // 최대 재시도 횟수
    long backoff() default 50;    // 재시도 사이의 대기 시간 (ms)
}
