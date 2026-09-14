package com.mopl.auth.filter;

import com.mopl.auth.dto.AuthUser;
import com.mopl.auth.jwt.AuthCookies;
import com.mopl.auth.jwt.JwtTokenProvider;
import com.mopl.auth.redis.TokenRedisService;
import com.mopl.core.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * "로그인 사용자 식별" 의 핵심.
 * 1) 쿠키에서 access token 추출
 * 2) 서명/만료 검증
 * 3) Redis 무효화 체크 (잠금/권한변경/탈퇴/로그아웃 시 여기서 걸러짐 -> "강제 로그아웃" 구현부)
 * 4) 통과하면 SecurityContext 에 AuthUser 를 principal 로 세팅
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws IOException, ServletException {
        try {
            extractAccessToken(request).ifPresent(this::authenticate);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        Claims claims = jwtTokenProvider.parseClaims(token);
        UUID userId = jwtTokenProvider.getUserId(claims);
        Instant issuedAt = jwtTokenProvider.getIssuedAt(claims);

        if (tokenRedisService.isInvalidated(userId, issuedAt)) {
            return;
        }

        String email = jwtTokenProvider.getEmail(claims);
        UserRole role = jwtTokenProvider.getRole(claims);
        AuthUser authUser = new AuthUser(userId, email, role);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        var authentication = new UsernamePasswordAuthenticationToken(authUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Optional<String> extractAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (AuthCookies.ACCESS_TOKEN.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}