package com.couchping.repository;

import com.couchping.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.couchping.model.ReservationStatus;
import java.time.LocalDate;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 유저의 예약 내역 조회
    List<Reservation> findAllByUserId(Long userId);

    // 숙소의 예약 내역 조회
    List<Reservation> findAllByRoomId(Long roomId);

    // 특정 숙소의 특정 기간에 상태가 CONFIRMED 인 예약이 존재하는지 (날짜 겹침 확인)
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Reservation r " +
           "WHERE r.roomId = :roomId " +
           "AND r.status = :status " +
           "AND r.checkInDate < :checkOutDate " +
           "AND r.checkOutDate > :checkInDate")
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("status") ReservationStatus status,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
