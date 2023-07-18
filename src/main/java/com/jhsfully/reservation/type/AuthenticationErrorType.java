package com.jhsfully.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthenticationErrorType {
    AUTHENTICATION_USER_NOT_FOUND("해당 유저를 찾을 수 없습니다."),
    AUTHENTICATION_USER_ALREADY_EXIST("해당 아이디는 이미 존재합니다."),
    AUTHENTICATION_USER_LOGIN_FAIL("로그인에 실패하였습니다.");

    private final String message;
}
