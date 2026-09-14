package com.mopl.auth.controller;

import com.mopl.auth.dto.LoginRequest;
import com.mopl.auth.jwt.AuthCookies;
import com.mopl.auth.jwt.JwtTokenProvider;
import com.mopl.auth.service.AuthService;
import com.mopl.user.dto.UserResponse;
import com.mopl.auth.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 경로(/api/auth/**)는 자료에 명시되지 않아 관례상 임의로 정했습니다. 실제 스펙 확정되면 맞춰주세요.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.TokenPair tokenPair = authService.login(request.email(), request.password());
        setAuthCookies(response, tokenPair.accessToken(), tokenPair.refreshToken());
        return tokenPair.user();
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        UUID userId = SecurityUtil.getCurrentUserId();
        authService.logout(userId);
        clearAuthCookies(response);
    }

    @PostMapping("/reissue")
    public void reissue(@CookieValue(AuthCookies.REFRESH_TOKEN) String refreshToken, HttpServletResponse response) {
        String newAccessToken = authService.reissueAccessToken(refreshToken);
        setAccessTokenCookie(response, newAccessToken);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAccessTokenCookie(response, accessToken);
        ResponseCookie refreshCookie = ResponseCookie.from(AuthCookies.REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(jwtTokenProvider.getRefreshTokenExpirationSeconds())
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie accessCookie = ResponseCookie.from(AuthCookies.ACCESS_TOKEN, accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from(AuthCookies.ACCESS_TOKEN, "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from(AuthCookies.REFRESH_TOKEN, "")
                .httpOnly(true).secure(true).sameSite("Strict").path("/api/auth").maxAge(0).build();
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}