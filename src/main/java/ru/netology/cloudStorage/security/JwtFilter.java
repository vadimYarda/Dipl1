package ru.netology.cloudStorage.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_TOKEN = "auth-token";
    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String token = getTokenFromRequest((HttpServletRequest) request);
        if (token != null) {
            log.info("Token found in request: {}", token);
            if (jwtProvider.validateAccessToken(token)) {
                log.info("Token is valid: {}", token);
                Claims claims = jwtProvider.getAccessClaims(token);
                JwtAuthentication jwtInfoToken = JwtUtils.generate(claims);
                jwtInfoToken.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
            } else {
                log.warn("Token is invalid: {}", token);
            }
        } else {
            log.warn("No token found in request.");
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION);
        String bearer_token = request.getHeader(AUTH_TOKEN);
        String token = bearer == null ? getTokenFromHeader(bearer_token) : getTokenFromHeader(bearer);
        log.info("Extracted token from request: {}", token);
        return token;
    }

    private String getTokenFromHeader(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            log.info("Extracted token from header: {}", token);
            return token;
        }
        return null;
    }
}
