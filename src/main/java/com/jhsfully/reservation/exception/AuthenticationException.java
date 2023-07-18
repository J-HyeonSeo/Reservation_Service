package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.AuthenticationErrorType;
import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException{
    private AuthenticationErrorType authenticationErrorType;
    private String message;

    public AuthenticationException(AuthenticationErrorType authenticationErrorType){
        this.authenticationErrorType = authenticationErrorType;
        this.message = authenticationErrorType.getMessage();
    }
}
