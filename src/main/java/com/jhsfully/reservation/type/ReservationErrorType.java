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
    RESERVATION_CANNOT_DELETE("삭제 불가능한 예약입니다."),
    RESERVATION_CANNOT_REJECT_NOT_READY("대기 중이지 않은 예약은 거절할 수 없습니다."),
    RESERVATION_CANNOT_REJECT_NOW_EQUAL_BEFORE("오늘을 포함한 이전 예약은 거절할 수 없습니다."),
    RESERVATION_CANNOT_ASSIGN_NOT_READY("대기 중이지 않은 예약은 승인할 수 없습니다."),
    RESERVATION_CANNOT_ASSIGN_NOW_EQUAL_BEFORE("오늘을 포함한 이전 예약은 승인할 수 없습니다."),
    RESERVATION_CANNOT_VISIT_DAY_NOT_EQUAL("방문일과 예약일이 맞지 않습니다."),
    RESERVATION_CANNOT_VISIT_TIME_OVER("예약 일시를 넘어서 방문이 불가능합니다.");
    private final String message;
}
