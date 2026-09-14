package com.mopl.auth.service;

import com.mopl.auth.jwt.JwtTokenProvider;
import com.mopl.auth.redis.TokenRedisService;
import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.auth.AuthErrorCode;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.domain.user.entity.User;
import com.mopl.user.dto.UserResponse;
import com.mopl.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    public record TokenPair(String accessToken, String refreshToken, UserResponse user) {
    }

    @Transactional(readOnly = true)
    public TokenPair login(String email, String rawPassword) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new MoplException(AuthErrorCode.INVALID_CREDENTIALS));

        if (user.getDeletedAt() != null) {
            throw new MoplException(UserErrorCode.USER_WITHDRAWN);
        }
        if (user.isLocked()) {
            throw new MoplException(UserErrorCode.USER_LOCKED);
        }
        if (!matchesPasswordOrTempPassword(user, rawPassword)) {
            throw new MoplException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    /** 비밀번호 또는 "만료되지 않은" 임시 비밀번호 중 하나라도 맞으면 통과. */
    private boolean matchesPasswordOrTempPassword(User user, String rawPassword) {
        if (user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            return true;
        }
        boolean tempPasswordStillValid = user.getTempPassword() != null
                && user.getTempPasswordExpiredAt() != null
                && user.getTempPasswordExpiredAt().isAfter(LocalDateTime.now());
        return tempPasswordStillValid && passwordEncoder.matches(rawPassword, user.getTempPassword());
    }

    private TokenPair issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);
        return new TokenPair(accessToken, refreshToken, UserResponse.from(user));
    }

    /** refresh token 검증 후 access token 재발급 (refresh token 은 재사용, rotate 하지 않음) */
    public String reissueAccessToken(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new MoplException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        var claims = jwtTokenProvider.parseClaims(refreshToken);
        UUID userId = jwtTokenProvider.getUserId(claims);

        if (!tokenRedisService.isValidRefreshToken(userId, refreshToken)) {
            throw new MoplException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new MoplException(AuthErrorCode.INVALID_CREDENTIALS));
        if (user.isLocked()) {
            throw new MoplException(UserErrorCode.USER_LOCKED);
        }

        return jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
    }

    /** 로그아웃: refresh token 삭제 + 지금까지 발급된 access token 전부 무효화 */
    public void logout(UUID userId) {
        tokenRedisService.deleteRefreshToken(userId);
        tokenRedisService.invalidateTokensIssuedBefore(userId, Instant.now());
    }
}