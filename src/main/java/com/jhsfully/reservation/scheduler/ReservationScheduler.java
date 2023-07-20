package com.jhsfully.reservation.scheduler;

/*
새벽마다, 예약 상태를 확인하고, 이를 다시 정정하는 스케줄러

READY상태가 예약 승인해야하는 날까지 승인이 안되었다면,
READY => REJECT 로 상태 변경 (파트너가 미승인했으므로 거절했다는 증거를 남김.)

ASSIGN상태가 예약시간까지, VISITED가 되지 않았다면,
ASSIGN => EXPIRE 로 상태 변경 (노쇼이므로, 사용자에게 좋지 않음)
*/

public class ReservationScheduler {
}
