package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.exception.RefreshTokenException;
import com.jhsfully.reservation.model.AuthDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.security.TokenProvider;
import com.jhsfully.reservation.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

import static com.jhsfully.reservation.type.AuthenticationErrorType.*;
import static com.jhsfully.reservation.type.RefreshTokenErrorType.REFRESH_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    //회원가입을 수행, username이 중복되지 말아야함.
    //파트너의 여부를 입력받아, 파트너에 대한 처리를 수행함.
    @Override
    public void signUp(AuthDto.SignUpRequest request, boolean isPartner) {
        int count = memberRepository.countByUsername(request.getUsername());

        if(count > 0){
            throw new AuthenticationException(AUTHENTICATION_USER_ALREADY_EXIST);
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .registeredAt(LocalDateTime.now())
                .isAdmin(false)
                .isPartner(isPartner)
                .build();

        memberRepository.save(member);
    }

    //로그인을 수행함.
    //accessToken과 refreshToken을 발급하여, 유저에게 반환해줌.
    /*
        accessToken : 접근을 위한 토큰이며, 권한을 가지고 있고, 생명 주기는 30분
        refreshToken : accessToken을 재발급하기 위한 토큰이며, 권한이 없고, 생명 주기는 2주

        토큰 발급 로직
        1. 로그인시, accessToken과 refreshToken을 발급하여 넘겨줌.
        2. 평소에는 accessToken만 사용하여, 통신을 수행함.
        3. accessToken의 기한이 만료될 경우, 401에러가 발생함.
        4. 이 때, /auth/refresh로 accessToken의 발급을 시도해서 accessToken을 받는다.
        5. 이 때, 401에러가 발생하면, 재 로그인이 필요한 경우이다.
        6. 아닌 경우, accessToken을 다시 할당하여, 요청을 수행한다.
     */
    @Override
    public AuthDto.SignInResponse signIn(AuthDto.SignInRequest request) {

        //보안 정책상, username, password 중 어느 부분이 틀렸는지 확인 할 수 없도록 함.
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException(AUTHENTICATION_USER_LOGIN_FAIL));

        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())){
            throw new AuthenticationException(AUTHENTICATION_USER_LOGIN_FAIL);
        }

        String accessToken = tokenProvider.generateAccessToken(member.getId(), member.isPartner(), member.isAdmin());
        String refreshToken = tokenProvider.generateRefreshToken(member.getId(), member.isPartner(), member.isAdmin());

        return new AuthDto.SignInResponse(member.getName(), accessToken, refreshToken);
    }

    //토큰을 리프레시함.
    @Override
    public AuthDto.refreshResponse tokenRefresh(String refreshToken) {
        String parsedRefreshToken = null;
        if(!ObjectUtils.isEmpty(refreshToken) && refreshToken.startsWith("Bearer ")){
            parsedRefreshToken = refreshToken.substring(7);
        }else{
            throw new RefreshTokenException(REFRESH_TOKEN_NOT_FOUND);
        }
        if(!tokenProvider.validateToken(parsedRefreshToken)){
            throw new AuthenticationException(AUTHENTICATION_UNAUTHORIZED);
        }
        String accessToken = tokenProvider.generateAccessTokenByRefresh(parsedRefreshToken);
        return new AuthDto.refreshResponse(accessToken);
    }


}
