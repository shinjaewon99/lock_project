package com.side.room_project.controller;

import com.side.room_project.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/optimistic/{roomId}")
    public String reserveOptimistic(@PathVariable Long roomId) {
        reservationService.reserveOptimistic(roomId);
        return "Reservation successful (Optimistic)";
    }

    @PostMapping("/pessimistic/{roomId}")
    public String reservePessimistic(@PathVariable Long roomId) {
        reservationService.reservePessimistic(roomId);
        return "Reservation successful (Pessimistic)";
    }
}
