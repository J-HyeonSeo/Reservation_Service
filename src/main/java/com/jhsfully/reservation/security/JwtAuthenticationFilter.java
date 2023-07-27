package com.jhsfully.reservation.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ACCESS_TOKEN_HEADER = "AccessToken";
    public static final String TOKEN_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isSkip(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveTokenFromRequest(request, ACCESS_TOKEN_HEADER);

        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {

            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else{ //로그인이 필요한 경우임.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
        log.info(requestURI);
        if(requestURI.startsWith("/auth/signin"))return true;
        if(requestURI.startsWith("/auth/user/signup"))return true;
        if(requestURI.startsWith("/auth/partner/signup"))return true;
        if(requestURI.startsWith("/auth/refresh"))return true;

        //for Develop
        if(requestURI.startsWith("/h2-console"))return true;
        if(requestURI.startsWith("/swagger"))return true;
        if(requestURI.startsWith("/v2"))return true;
        return false;
    }
}
