package com.side.room_project.facade;

import com.side.room_project.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockFacade {

    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    public void reserveWithLock(Long roomId) {
        RLock lock = redissonClient.getLock(roomId.toString());

        try {
            // 1. 락 획득 시도 (최대 10초 대기, 1초 동안 락 유지)
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                log.info("lock 획득 실패");
                return;
            }

            // 2. 비즈니스 로직 실행
            reservationService.reserve(roomId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 3. 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
