package com.side.room_project.repository;

import com.side.room_project.domain.MeetingRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    // 1단계 테스트용: 비관적 락 (DB 자체에 X-Lock을 검)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MeetingRoom m where m.id = :id")
    Optional<MeetingRoom> findByIdWithPessimisticLock(@Param("id") Long id);

    // 2단계 테스트용: 낙관적 락 (어플리케이션 단에서 Version으로 검증)
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select m from MeetingRoom m where m.id = :id")
    Optional<MeetingRoom> findByIdWithOptimisticLock(@Param("id") Long id);
}
