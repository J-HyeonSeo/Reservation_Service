package com.jhsfully.reservation.exception;

import com.jhsfully.reservation.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_UNAUTHORIZED;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> inputArgsExceptionHandler(BindingResult bindingResult){
        String message = bindingResult.getFieldError().getDefaultMessage();

        if(message == null){
            message = "요청된 값이 올바르지 않습니다.";
        }

        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customExceptionHandler(CustomException e){

        //인증 되지 않은 사용자
        if(e instanceof AuthenticationException){
            AuthenticationException authenticationException = (AuthenticationException)e;
            if(AUTHENTICATION_UNAUTHORIZED == authenticationException.getAuthenticationErrorType()){
                return ResponseEntity.status(401).body(
                        new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                e.getMessage()
                        )
                );
            }
        }

        return ResponseEntity.internalServerError().body(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
        );
    }

}
