package com.jhsfully.reservation.type;

public enum ReservationState {
    /*
    READY : 예약 확정 대기
    ASSIGN : 예약 확정
    VISITED : 방문 확인된 예약(리뷰 작성 가능 상태)
    REJECT : 예약 거절
    EXPIRED : 예약을 확정 했으나, 노쇼이거나, 취소한 상태(예약 파기 상태)
     */
    READY, ASSIGN, VISITED, REJECT, EXPIRED
}
