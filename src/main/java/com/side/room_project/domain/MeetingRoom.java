package com.side.room_project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MeetingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    private int availableSeats;

    // 낙관적 락의 핵심. 변경될 때마다 자동으로 1씩 증가
    @Version
    private Long version;

    public MeetingRoom(String roomName, int availableSeats) {
        this.roomName = roomName;
        this.availableSeats = availableSeats;
    }

    public void reserveSeat() {
        if (this.availableSeats <= 0) {
            throw new RuntimeException("예약이 마감되었습니다.");
        }
        this.availableSeats--;
    }
}
