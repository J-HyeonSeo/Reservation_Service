package com.jhsfully.reservation.service.impl;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.exception.AuthenticationException;
import com.jhsfully.reservation.model.AuthDto;
import com.jhsfully.reservation.repository.MemberRepository;
import com.jhsfully.reservation.security.TokenProvider;
import com.jhsfully.reservation.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_ALREADY_EXIST;
import static com.jhsfully.reservation.type.AuthenticationErrorType.AUTHENTICATION_USER_LOGIN_FAIL;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

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
}
