package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.AuthDto;

public interface AuthService {

    void signUp(AuthDto.SignUpRequest request, boolean isPartner);
    AuthDto.SignInResponse signIn(AuthDto.SignInRequest request);

}
