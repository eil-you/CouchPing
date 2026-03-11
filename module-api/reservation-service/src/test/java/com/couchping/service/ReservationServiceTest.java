package com.couchping.service;

import com.couchping.entity.Reservation;
import com.couchping.exception.CouchPingException;
import com.couchping.model.ReservationErrorCode;
import com.couchping.model.ReservationRequest;
import com.couchping.model.ReservationStatus;
import com.couchping.repository.ReservationRepository;
import com.couchping.entity.Room;
import com.couchping.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

        @Mock
        private ReservationRepository reservationRepository;

        @Mock
        private RoomRepository roomRepository;

        @InjectMocks
        private ReservationService reservationService;

        @Test
        @DisplayName("숙소 예약 생성 (성공)")
        void createReservation_Success() {
                // given
                ReservationRequest request = new ReservationRequest(1L, 1L, LocalDate.now(),
                                LocalDate.now().plusDays(1), 100000);

                // when
                reservationService.createReservation(request);

                // then
                verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        @DisplayName("즉시 예약 생성 (성공)")
        void createInstantReservation_Success() {
                // given
                Long roomId = 1L;
                ReservationRequest request = new ReservationRequest(1L, roomId, LocalDate.now(),
                                LocalDate.now().plusDays(1), 100000);
                
                Room room = Room.builder().hostId(2L).build();
                room.updateInstantBook(true);

                given(roomRepository.findById(roomId)).willReturn(Optional.of(room));
                given(reservationRepository.existsConflictingReservation(anyLong(), any(), any(), any())).willReturn(false);

                // when
                reservationService.createInstantReservation(request);

                // then
                // In ReservationService, the created reservation is saved with CONFIRMED status.
                // We verify that save was called.
                verify(reservationRepository, times(1)).save(argThat(res -> res.getStatus() == ReservationStatus.CONFIRMED));
        }

        @Test
        @DisplayName("즉시 예약 생성 실패 - 즉시 예약 허용 안됨")
        void createInstantReservation_Fail_NotAllowed() {
                // given
                Long roomId = 1L;
                ReservationRequest request = new ReservationRequest(1L, roomId, LocalDate.now(),
                                LocalDate.now().plusDays(1), 100000);
                
                Room room = Room.builder().hostId(2L).build();
                room.updateInstantBook(false);

                given(roomRepository.findById(roomId)).willReturn(Optional.of(room));

                // when & then
                CouchPingException ex = assertThrows(CouchPingException.class, () -> reservationService.createInstantReservation(request));
                assertEquals(ReservationErrorCode.INSTANT_BOOK_NOT_ALLOWED, ex.getErrorCode());
        }

        @Test
        @DisplayName("즉시 예약 생성 실패 - 날짜 중복 (더블 부킹)")
        void createInstantReservation_Fail_DoubleBooking() {
                // given
                Long roomId = 1L;
                ReservationRequest request = new ReservationRequest(1L, roomId, LocalDate.now(),
                                LocalDate.now().plusDays(1), 100000);
                
                Room room = Room.builder().hostId(2L).build();
                room.updateInstantBook(true);

                given(roomRepository.findById(roomId)).willReturn(Optional.of(room));
                // 중복 발생
                given(reservationRepository.existsConflictingReservation(anyLong(), any(), any(), any())).willReturn(true);

                // when & then
                CouchPingException ex = assertThrows(CouchPingException.class, () -> reservationService.createInstantReservation(request));
                assertEquals(ReservationErrorCode.DOUBLE_BOOKING, ex.getErrorCode());
        }

        @Test
        @DisplayName("수동 예약 승인 (성공)")
        void confirmReservation_Success() {
                // given
                Long reservationId = 1L;
                Reservation reservation = Reservation.builder()
                        .roomId(1L)
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(1))
                        .build();
                
                given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
                given(reservationRepository.existsConflictingReservation(anyLong(), any(), any(), any())).willReturn(false);

                // when
                reservationService.confirmReservation(reservationId);

                // then
                assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        }

        @Test
        @DisplayName("수동 예약 승인 실패 - 날짜 중복 (더블 부킹)")
        void confirmReservation_Fail_DoubleBooking() {
                // given
                Long reservationId = 1L;
                Reservation reservation = Reservation.builder()
                        .roomId(1L)
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(1))
                        .build();
                
                given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
                given(reservationRepository.existsConflictingReservation(anyLong(), any(), any(), any())).willReturn(true);

                // when & then
                CouchPingException ex = assertThrows(CouchPingException.class, () -> reservationService.confirmReservation(reservationId));
                assertEquals(ReservationErrorCode.DOUBLE_BOOKING, ex.getErrorCode());
        }

        @Test
        @DisplayName("숙소 예약 취소 (성공)")
        void cancelReservation_Success() {
                // given
                Long reservationId = 1L;
                Reservation reservation = Reservation.builder()
                                .userId(1L)
                                .roomId(1L)
                                .checkInDate(LocalDate.now())
                                .checkOutDate(LocalDate.now().plusDays(1))
                                .totalPrice(100000)
                                .build();

                given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

                // when
                reservationService.cancelReservation(reservationId);

                // then
                assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        }

        @Test
        @DisplayName("숙소 삭제 시 모든 확정된 예약 취소 (성공)")
        void cancelAllByRoomId_Success() {
                // given
                Long roomId = 1L;
                Reservation res1 = Reservation.builder().roomId(roomId).build();
                res1.updateStatus(ReservationStatus.CONFIRMED);
                Reservation res2 = Reservation.builder().roomId(roomId).build();
                res2.updateStatus(ReservationStatus.CONFIRMED);

                given(reservationRepository.findAllByRoomId(roomId)).willReturn(List.of(res1, res2));

                // when
                reservationService.cancelAllByRoomId(roomId);

                // then
                assertEquals(ReservationStatus.CANCELLED, res1.getStatus());
                assertEquals(ReservationStatus.CANCELLED, res2.getStatus());
        }

        @Test
        @DisplayName("유저별 예약 내역 조회")
        void getReservationsByUserId_Success() {
                // given
                Long userId = 1L;
                Reservation reservation = Reservation.builder()
                                .userId(userId)
                                .roomId(1L)
                                .checkInDate(LocalDate.now())
                                .checkOutDate(LocalDate.now().plusDays(1))
                                .totalPrice(100000)
                                .build();

                given(reservationRepository.findAllByUserId(userId)).willReturn(List.of(reservation));

                // when
                List<Reservation> results = reservationService.getReservationsByUserId(userId);

                // then
                assertEquals(1, results.size());
                assertEquals(userId, results.get(0).getUserId());
        }
}
