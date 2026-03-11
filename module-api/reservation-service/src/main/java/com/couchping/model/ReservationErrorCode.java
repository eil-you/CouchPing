package com.couchping.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
public enum ReservationErrorCode implements BaseErrorCode {

    ROOM_NOT_AVAILABLE(2001, "Room not available for selected dates", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_FOUND(2002, "Reservation not found", HttpStatus.NOT_FOUND),
    INVALID_DATE(2003, "Invalid date", HttpStatus.BAD_REQUEST),
    DOUBLE_BOOKING(2004, "Double booking", HttpStatus.CONFLICT),
    INSTANT_BOOK_NOT_ALLOWED(2005, "This room does not allow instant booking", HttpStatus.BAD_REQUEST),
    GUEST_RATING_TOO_LOW(2006, "Guest rating must be at least 3.0 for instant booking", HttpStatus.FORBIDDEN),
    SYSTEM_ERROR(2007, "System error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    @NonNull
    private final HttpStatus httpStatus;

}
