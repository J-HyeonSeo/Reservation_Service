package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.RefreshTokenErrorType;
import lombok.Getter;

@Getter
public class RefreshTokenException extends CustomException{
    private final RefreshTokenErrorType refreshTokenErrorType;

    public RefreshTokenException(RefreshTokenErrorType refreshTokenErrorType){
        super(refreshTokenErrorType.getMessage());
        this.refreshTokenErrorType = refreshTokenErrorType;
    }
}
