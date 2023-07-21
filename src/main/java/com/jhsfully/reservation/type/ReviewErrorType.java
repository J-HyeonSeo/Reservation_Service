package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorType {
    REVIEW_NOT_FOUND("해당 예약이 존재하지 않습니다."),
    REVIEW_NOT_MATCH_USER("해당 회원의 리뷰가 아닙니다."),
    REVIEW_STATE_NOT_VISITED("방문된 상태의 리뷰만 작성이 가능합니다."),
    REVIEW_ALREADY_WRITTEN("이미 리뷰가 작성되어있습니다."),
    REVIEW_TIME_OVER("방문일로 부터 일주일 안에만 리뷰를 작성할 있습니다.");
    private final String message;
}
