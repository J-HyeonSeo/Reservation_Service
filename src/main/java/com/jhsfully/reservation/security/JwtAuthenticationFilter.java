package com.jhsfully.reservation.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ACCESS_TOKEN_HEADER = "AccessToken";
    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    public static final String TOKEN_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isSkip(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveTokenFromRequest(request, ACCESS_TOKEN_HEADER);
        String refreshToken = resolveTokenFromRequest(request, REFRESH_TOKEN_HEADER);

        try {
            if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {

                Authentication authentication = tokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else if (StringUtils.hasText(refreshToken) &&
                    tokenProvider.validateToken(refreshToken)) {
                String newAccessToken = tokenProvider.generateAccessTokenByRefresh(refreshToken);
                response.addHeader("accessToken", newAccessToken);
            }else{
                throw new RuntimeException();
            }
        }catch (Exception e){ //로그인이 필요한 경우임.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("message", "로그인이 필요합니다.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request, String tokenHeader){
        String token = request.getHeader(tokenHeader);

        if(!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)){
            return token.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private boolean isSkip(String requestURI){
        if(requestURI.startsWith("/auth/signin"))return true;
        if(requestURI.startsWith("/auth/user/register"))return true;
        if(requestURI.startsWith("/auth/partner/register"))return true;
        return false;
    }
}
