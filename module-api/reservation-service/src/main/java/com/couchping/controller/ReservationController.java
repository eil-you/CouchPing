package com.couchping.controller;

import com.couchping.model.ReservationRequest;
import com.couchping.service.ReservationService;
import com.couchping.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 일반 예약 요청 (PENDING)
    @PostMapping
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequest reservationRequest) {
        reservationService.createReservation(reservationRequest);
        return ResponseEntity.ok("Reservation requested successfully");
    }

    // 우수 게스트 프리패스 (즉시 예약, 결제 동시 진행)
    @PostMapping("/instant")
    public ResponseEntity<String> createInstantReservation(@RequestBody ReservationRequest reservationRequest) {
        reservationService.createInstantReservation(reservationRequest);
        return ResponseEntity.ok("Instant reservation created successfully");
    }

    // 수동 예약 승인 (호스트)
    @PutMapping("/{reservationId}/confirm")
    public ResponseEntity<String> confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok("Reservation confirmed successfully");
    }

    // 유저(게스트)별 전체 예약 리스트 조회
    @GetMapping("/{userId}")
    public ResponseEntity<List<Reservation>> getReservationsByUserId(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(reservations);
    }

    // 예약 취소
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("Reservation cancelled successfully");
    }
}
