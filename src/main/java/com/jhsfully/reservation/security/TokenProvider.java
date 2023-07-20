package com.jhsfully.reservation.security;

import com.jhsfully.reservation.domain.RefreshToken;
import com.jhsfully.reservation.exception.RefreshTokenException;
import com.jhsfully.reservation.repository.RefreshTokenRepository;
import com.jhsfully.reservation.type.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.jhsfully.reservation.type.RefreshTokenErrorType.REFRESH_TOKEN_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    //JWT는 AccessToken과 RefreshToken으로 구별할거임.
    //AccessToekn의 생명주기는 30분, RefreshToken의 생명주기는 2주로 잡음.

    @Value("${spring.jwt.secret}")
    private String secretKey;
    private static final String MEMBER_ID = "memberId";
    private static final String IS_PARTNER = "partner";
    private static final String IS_ADMIN = "admin";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30 * 240;//1초 -> 1분 -> 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14;//1초 -> 1분 -> 1시간 -> 1일 -> 2주

    private final RefreshTokenRepository refreshTokenRepository;


    //Access 토큰 생성
    public String generateAccessToken(Long memberId, boolean isPartner, boolean isAdmin){
        Claims claims = Jwts.claims();
        claims.put(MEMBER_ID, memberId);
        claims.put(IS_PARTNER, isPartner);
        claims.put(IS_ADMIN, isAdmin);

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    //Refresh토큰으로 AccessToken생성
    public String generateAccessTokenByRefresh(String refreshToken){

        //redis에 refreshToken이 등록되어있는지 확인.
        RefreshToken refreshTokenEntity = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new RefreshTokenException(REFRESH_TOKEN_NOT_FOUND));

        //memberId 가져오기
        Long memberId = getMemberId(refreshToken);

        //refreshToken에서 isAdmin과 isPartner를 가져올 수 있음.

        return generateAccessToken(memberId, refreshTokenEntity.isPartner(), refreshTokenEntity.isAdmin());
    }

    //Refresh 토큰 생성
    public String generateRefreshToken(Long memberId, boolean isPartner, boolean isAdmin){
        Claims claims = Jwts.claims();
        claims.put(MEMBER_ID, memberId);

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        //추후에 REDIS 저장 로직 구현해야함.

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    //토큰을 통해, 인증 객체 생성.
    public Authentication getAuthentication(String token){

        Long memberId = getMemberId(token);
        List<String> roles = getRoles(token);

        List<SimpleGrantedAuthority> grantedAuthorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(memberId, "", grantedAuthorities);
    }

    //토큰으로부터 권한 가져오기.
    private List<String> getRoles(String token){
        Claims claims = parseClaims(token);

        boolean isPartner = claims.get(IS_PARTNER, Boolean.class);
        boolean isAdmin = claims.get(IS_ADMIN, Boolean.class);

        List<String> roles = new ArrayList<>();

        if(isPartner){
            roles.add(RoleType.ROLE_PARTNER.name());
        }else{
            roles.add(RoleType.ROLE_USER.name());
        }

        if(isAdmin){
            roles.add(RoleType.ROLE_ADMIN.name());
        }

        return roles;
    }

    //회원 번호 가져오기.
    private Long getMemberId(String token){
        return this.parseClaims(token).get(MEMBER_ID, Long.class);
    }

    //토큰 유효기간 검증.
    public boolean validateToken(String token){
        if(!StringUtils.hasText(token)){
            return false;
        }

        Claims claims = parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    //토큰 파싱
    private Claims parseClaims(String token){
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

}
