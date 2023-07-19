package com.jhsfully.reservation.controller;

import com.jhsfully.reservation.model.AuthDto;
import com.jhsfully.reservation.service.AuthService;
import io.netty.channel.unix.Errors;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/user/signup")
    public ResponseEntity<?> userSignUp(@RequestBody @Valid AuthDto.SignUpRequest request)
    {
        authService.signUp(request, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/partner/signup")
    public ResponseEntity<?> partnerSignUp(@RequestBody @Valid AuthDto.SignUpRequest request){
        authService.signUp(request, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthDto.SignInResponse> signIn(
            @RequestBody @Valid AuthDto.SignInRequest request
    ){
        return ResponseEntity.ok(authService.signIn(request));
    }
}
