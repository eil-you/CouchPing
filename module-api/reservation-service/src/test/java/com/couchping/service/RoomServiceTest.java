package com.couchping.service;

import com.couchping.entity.Room;
import com.couchping.exception.CouchPingException;
import com.couchping.model.*;
import com.couchping.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private RoomService roomService;

    @Test
    @DisplayName("숙박 시설 등록 성공")
    void registerRoom_Success() {
        // given
        Long hostId = 1L;
        RoomRequest request = createRoomRequest("테스트 숙소");
        Room room = request.toEntity(hostId);

        // Room은 ID에 대한 setter가 없으므로, JpaRepository의 save가 ID가 있는 객체를 반환하도록 설정합니다.
        given(roomRepository.save(any(Room.class))).willReturn(room);

        // when
        Long roomId = roomService.registerRoom(hostId, request);

        // then
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("숙박 시설 정보 수정 성공")
    void updateRoom_Success() {
        // given
        Long hostId = 1L;
        Long roomId = 10L;
        Room existingRoom = createRoom(hostId, "기존 숙소");
        RoomRequest updateRequest = createRoomRequest("수정된 숙소");

        given(roomRepository.findById(roomId)).willReturn(Optional.of(existingRoom));

        // when
        roomService.updateRoom(hostId, roomId, updateRequest);

        // then
        assertEquals("수정된 숙소", existingRoom.getTitle());
    }

    @Test
    @DisplayName("숙소 정보 수정 실패 - 권한 없음")
    void updateRoom_Fail_Unauthorized() {
        // given
        Long hostId = 1L;
        Long otherHostId = 2L;
        Long roomId = 10L;
        Room existingRoom = createRoom(otherHostId, "다른 사람 숙소");
        RoomRequest updateRequest = createRoomRequest("수정 시도");

        given(roomRepository.findById(roomId)).willReturn(Optional.of(existingRoom));

        // when & then
        assertThrows(CouchPingException.class, () -> roomService.updateRoom(hostId, roomId, updateRequest));
    }

    @Test
    @DisplayName("숙소 활성화 상태 전환 성공")
    void toggleActiveStatus_Success() {
        // given
        Long hostId = 1L;
        Long roomId = 10L;
        Room room = createRoom(hostId, "테스트 숙소");
        assertTrue(room.isActive());

        given(roomRepository.findById(roomId)).willReturn(Optional.of(room));

        // when
        roomService.toggleActiveStatus(hostId, roomId, false);

        // then
        assertFalse(room.isActive());
    }

    @Test
    @DisplayName("숙소 삭제 성공 - 활성 예약 취소 포함")
    void deleteRoom_Success() {
        // given
        Long hostId = 1L;
        Long roomId = 10L;
        Room room = createRoom(hostId, "삭제될 숙소");

        given(roomRepository.findById(roomId)).willReturn(Optional.of(room));

        // when
        roomService.deleteRoom(hostId, roomId);

        // then
        assertTrue(room.isDeleted());
        assertFalse(room.isActive());
        verify(reservationService, times(1)).cancelAllByRoomId(roomId);
    }

    // Helper Methods
    private RoomRequest createRoomRequest(String title) {
        return new RoomRequest(
                title, "설명", "url", "위치", 10000, 2, "체크인 방법",
                RoomType.PRIVATE_ROOM, PreferredGender.ANY, false, new ArrayList<>());
    }

    private Room createRoom(Long hostId, String title) {
        return Room.builder()
                .hostId(hostId)
                .title(title)
                .description("숙소 설명")
                .imageUrl("url")
                .location("서울시 강남구")
                .price(10000)
                .capacity(2)
                .initialQuestion("게스트에게 묻고 싶은 질문")
                .roomType(RoomType.PRIVATE_ROOM)
                .preferredGender(PreferredGender.ANY)
                .build();
    }
}
