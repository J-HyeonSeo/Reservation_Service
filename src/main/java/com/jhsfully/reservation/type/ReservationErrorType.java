package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorType {
    RESERVATION_NOT_FOUND("해당 예약이 존재하지 않습니다."),
    RESERVATION_NOT_MATCH_USER("해당 예약의 사용자가 아닙니다."),
    RESERVATION_NOT_OPENED_DAY("해당 날은 오픈되지 않은 예약일입니다."),
    RESERVATION_NOT_OPENED_TIME("해당 시간대는 오픈되지 않았습니다."),
    RESERVATION_IS_OVERFLOW("예약이 초과되어 신청이 불가합니다."),
    RESERVATION_CANNOT_DELETE("삭제 불가능한 예약입니다.");
    private final String message;
}
