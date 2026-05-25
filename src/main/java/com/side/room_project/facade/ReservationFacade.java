package com.side.room_project.facade;

import com.side.room_project.common.annotation.OptimisticRetry;
import com.side.room_project.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;

    // 여기에만 AOP 어노테이션을 붙입니다. (트랜잭션 바깥쪽)
    @OptimisticRetry(maxRetries = 100, backoff = 50)
    public void reserveWithRetry(Long roomId) {
        // 내부에서 @Transactional이 걸린 실제 비즈니스 로직 호출
        // 실패하면 여기서 낚아채서 50ms 쉬고 '새로운 트랜잭션'으로 다시 호출함
        reservationService.reserveOptimistic(roomId);
    }
}
