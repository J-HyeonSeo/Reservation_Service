package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.type.AuthenticationErrorType;
import lombok.Getter;

@Getter
public class AuthenticationException extends CustomException{
    private final AuthenticationErrorType authenticationErrorType;

    public AuthenticationException(AuthenticationErrorType authenticationErrorType){
        super(authenticationErrorType.getMessage());
        this.authenticationErrorType = authenticationErrorType;
    }
}
