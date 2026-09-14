package com.mopl.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 에 아래와 같이 설정 필요:
 *
 * jwt:
 *   secret: {최소 256bit 이상의 랜덤 문자열, 환경변수/시크릿 매니저로 관리 권장}
 *   access-token-expiration-seconds: 1800     # 30분
 *   refresh-token-expiration-seconds: 1209600 # 14일
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds
) {
}