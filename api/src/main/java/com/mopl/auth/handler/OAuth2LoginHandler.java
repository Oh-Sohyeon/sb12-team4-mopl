package com.mopl.auth.handler;

import com.mopl.auth.exception.SocialLoginException;
import com.mopl.auth.jwt.AuthCookieWriter;
import com.mopl.auth.jwt.JwtTokenProvider;
import com.mopl.auth.redis.TokenRedisService;
import com.mopl.auth.service.SocialAuthService;
import com.mopl.core.common.enums.SocialProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 구글 로그인 성공/실패 처리.
 *
 * 일반 로그인(LoginSuccessHandler)은 JSON 으로 응답하지만, 소셜 로그인은 구글에서 브라우저가 우리 콜백 주소로
 * 직접 리다이렉트되어 돌아오는 흐름이라 JSON 응답을 받아줄 곳이 없다. 그래서 성공하면 JWT 쿠키를 심고
 * 프론트 주소로 리다이렉트하고, 실패하면 에러 코드를 쿼리스트링(?error=)에 담아 리다이렉트한다.
 *
 * app.oauth2.frontend-redirect-url (환경변수 APP_OAUTH2_FRONTEND_REDIRECT_URL) 은 프론트 담당자와
 * 확정이 필요한 값이라 기본값을 "/" 로 뒀다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final AuthCookieWriter cookieWriter;

    @Value("${app.oauth2.frontend-redirect-url:/}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        // 지금은 구글만 지원. 카카오/네이버는 응답 속성 구조가 달라서 별도 처리가 필요함
        if (!(authentication instanceof OAuth2AuthenticationToken token)
                || !"google".equals(token.getAuthorizedClientRegistrationId())) {
            redirectWithError(response, "OAUTH2_UNSUPPORTED_PROVIDER");
            return;
        }

        OAuth2User oAuth2User = token.getPrincipal();
        String providerId = oAuth2User.getAttribute("sub");
        if (providerId == null) {
            redirectWithError(response, "OAUTH2_LOGIN_FAILED");
            return;
        }

        try {
            SocialAuthService.SocialLoginResult result = socialAuthService.loginOrRegister(
                    SocialProvider.GOOGLE,
                    providerId,
                    oAuth2User.getAttribute("email"),
                    Boolean.TRUE.equals(oAuth2User.getAttribute("email_verified")),
                    oAuth2User.getAttribute("name"),
                    oAuth2User.getAttribute("picture")
            );

            String accessToken = jwtTokenProvider.createAccessToken(result.userId(), result.email(), result.role());
            String refreshToken = jwtTokenProvider.createRefreshToken(result.userId());
            tokenRedisService.saveRefreshToken(result.userId(), refreshToken);
            cookieWriter.setAccessTokenCookie(response, accessToken);
            cookieWriter.setRefreshTokenCookie(response, refreshToken);

            response.sendRedirect(frontendRedirectUrl);
        } catch (SocialLoginException e) {
            log.info("소셜 로그인 거절: {}", e.getErrorCode().getCode());
            redirectWithError(response, e.getErrorCode().getCode());
        }
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception
    ) throws IOException {
        log.warn("소셜 로그인 실패: {}", exception.getMessage());
        redirectWithError(response, "OAUTH2_LOGIN_FAILED");
    }

    private void redirectWithError(HttpServletResponse response, String errorCode) throws IOException {
        response.sendRedirect(
                UriComponentsBuilder.fromUriString(frontendRedirectUrl).queryParam("error", errorCode).build().toUriString()
        );
    }
}