package com.side.room_project;

import com.side.room_project.domain.MeetingRoom;
import com.side.room_project.facade.RedissonLockFacade;
import com.side.room_project.repository.MeetingRoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedissonLockTest {

    @Autowired
    private RedissonLockFacade redissonLockFacade;

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    private Long roomId;

    @BeforeEach
    void setUp() {
        // 좌석 100개가 있는 회의실 생성
        MeetingRoom room = new MeetingRoom("회의실 A", 100);
        roomId = meetingRoomRepository.saveAndFlush(room).getId();
    }

    @AfterEach
    void tearDown() {
        meetingRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 100명이 예약할 때, Redisson 분산 락으로 정합성을 보장한다.")
    void reserveWith100Threads() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockFacade.reserveWithLock(roomId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        MeetingRoom room = meetingRoomRepository.findById(roomId).orElseThrow();
        // 100개 좌석이 모두 예약되어 0이 되어야 함
        assertThat(room.getAvailableSeats()).isEqualTo(0);
    }
}
