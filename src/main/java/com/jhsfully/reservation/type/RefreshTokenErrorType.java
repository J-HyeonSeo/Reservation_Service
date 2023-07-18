package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefreshTokenErrorType {
    REFRESH_TOKEN_NOT_FOUND("토큰을 찾을 수 없습니다.");
    private final String message;
}
