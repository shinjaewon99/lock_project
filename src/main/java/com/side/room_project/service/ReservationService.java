package com.side.room_project.service;

import com.side.room_project.domain.MeetingRoom;
import com.side.room_project.repository.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final MeetingRoomRepository meetingRoomRepository;

    // AOP 어노테이션을 Facade로 이동시켰으므로 여기서는 제거합니다.
    @Transactional
    public void reserveOptimistic(Long roomId) {
        MeetingRoom room = meetingRoomRepository.findByIdWithOptimisticLock(roomId)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        room.reserveSeat();
    }

    @Transactional
    public void reservePessimistic(Long roomId) {
        MeetingRoom room = meetingRoomRepository.findByIdWithPessimisticLock(roomId)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        room.reserveSeat();
    }

    @Transactional
    public void reserve(Long roomId) {
        MeetingRoom room = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("회의실을 찾을 수 없습니다."));
        room.reserveSeat();
    }
}
