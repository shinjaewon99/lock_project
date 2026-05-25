package com.side.room_project;

import com.side.room_project.domain.MeetingRoom;
import com.side.room_project.facade.ReservationFacade;
import com.side.room_project.repository.MeetingRoomRepository;
import com.side.room_project.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    private Long testRoomId;

    @BeforeEach
    void setUp() {
        MeetingRoom room = new MeetingRoom("대회의실", 100);
        MeetingRoom savedRoom = meetingRoomRepository.save(room);
        testRoomId = savedRoom.getId();
    }

    @AfterEach
    void tearDown() {
        meetingRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락: 100명이 동시에 예약하면 정확히 100석이 소진되어야 한다.")
    void testPessimisticLock() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.reservePessimistic(testRoomId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        MeetingRoom findRoom = meetingRoomRepository.findById(testRoomId).orElseThrow();
        assertThat(findRoom.getAvailableSeats()).isEqualTo(0);
    }

    @Test
    @DisplayName("낙관적 락: 100명이 동시에 예약하면 충돌이 발생해 실패하는 요청이 생겨야 한다.")
    void testOptimisticLockFailure() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failedCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.reserveOptimistic(testRoomId);
                } catch (ObjectOptimisticLockingFailureException e) {
                    failedCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        MeetingRoom findRoom = meetingRoomRepository.findById(testRoomId).orElseThrow();
        assertThat(findRoom.getAvailableSeats()).isGreaterThan(0);
        assertThat(failedCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("낙관적 락 재시도: 100명이 동시에 접근해도 AOP가 재시도하여 100석이 모두 소진되어야 한다.")
    void testOptimisticLockWithRetry() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationFacade.reserveWithRetry(testRoomId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        MeetingRoom findRoom = meetingRoomRepository.findById(testRoomId).orElseThrow();
        
        System.out.println("====== 재시도 로직 적용 결과 ======");
        System.out.println("남은 좌석 수: " + findRoom.getAvailableSeats());

        assertThat(findRoom.getAvailableSeats()).isEqualTo(0);
    }
}
