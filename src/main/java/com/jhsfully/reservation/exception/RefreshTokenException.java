package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.RefreshTokenErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RefreshTokenException extends RuntimeException{
    private RefreshTokenErrorType refreshTokenErrorType;
    private String message;

    public RefreshTokenException(RefreshTokenErrorType refreshTokenErrorType){
        this.refreshTokenErrorType = refreshTokenErrorType;
        this.message = refreshTokenErrorType.getMessage();
    }
}
