package com.side.room_project.common.aop;

import com.side.room_project.common.annotation.OptimisticRetry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OptimisticRetryAspect {

    @Around("@annotation(optimisticRetry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, OptimisticRetry optimisticRetry) throws Throwable {
        int maxRetries = optimisticRetry.maxRetries();
        long backoff = optimisticRetry.backoff();
        Exception exceptionHolder = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return joinPoint.proceed();
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("낙관적 락 충돌 발생! 재시도 중... ({} / {})", i + 1, maxRetries);
                exceptionHolder = e;
                Thread.sleep(backoff); // 잠시 대기 후 재시도
            }
        }
        throw exceptionHolder;
    }
}
