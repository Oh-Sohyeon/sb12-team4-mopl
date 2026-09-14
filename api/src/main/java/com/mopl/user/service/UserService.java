package com.mopl.user.service;

import com.mopl.auth.redis.TokenRedisService;
import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.common.enums.UserRole;
import com.mopl.core.domain.user.entity.User;
import com.mopl.user.dto.CursorPageResponse;
import com.mopl.user.dto.UserResponse;
import com.mopl.user.dto.UserSearchCondition;
import com.mopl.user.repository.UserRepository;
import com.mopl.user.repository.UserSpecification;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TokenRedisService tokenRedisService;

    // ===================== 조회 =====================

    public UserResponse getUser(UUID userId) {
        User user = findActiveUserOrThrow(userId);
        return UserResponse.from(user);
    }

    public CursorPageResponse<UserResponse> getUsers(UserSearchCondition condition) {
        String sortField = UserSpecification.validateSortField(condition.sortBy());
        UUID idAfter = parseUuidOrNull(condition.idAfter());

        Specification<User> spec = Specification.allOf(
                UserSpecification.notDeleted(),
                UserSpecification.emailLike(condition.emailLike()),
                UserSpecification.roleEqual(condition.roleEqual()),
                UserSpecification.isLocked(condition.isLocked()),
                UserSpecification.cursorAfter(sortField, condition.sortDirection(), condition.cursor(), idAfter)
        );

        Sort.Direction direction = condition.sortDirection() == UserSearchCondition.SortDirection.ASCENDING
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

        // hasNext 판단을 위해 limit + 1 로 조회
        int limit = condition.limit();
        List<User> fetched = userRepository.findAll(spec, PageRequest.of(0, limit + 1, sort)).getContent();

        boolean hasNext = fetched.size() > limit;
        List<User> pageContent = hasNext ? fetched.subList(0, limit) : fetched;

        List<UserResponse> data = pageContent.stream().map(UserResponse::from).toList();

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !pageContent.isEmpty()) {
            User last = pageContent.getLast();
            nextCursor = extractCursorValue(last, sortField);
            nextIdAfter = last.getId().toString();
        }

        long totalCount = userRepository.count(Specification.allOf(
                UserSpecification.notDeleted(),
                UserSpecification.emailLike(condition.emailLike()),
                UserSpecification.roleEqual(condition.roleEqual()),
                UserSpecification.isLocked(condition.isLocked())
        ));

        return new CursorPageResponse<>(
                data, nextCursor, nextIdAfter, hasNext, totalCount,
                sortField, condition.sortDirection().name()
        );
    }

    private UUID parseUuidOrNull(String value) {
        return (value == null || value.isBlank()) ? null : UUID.fromString(value);
    }

    private String extractCursorValue(User user, String sortBy) {
        return switch (sortBy) {
            case "name" -> user.getName();
            case "email" -> user.getEmail();
            case "createdAt" -> user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            default -> throw new IllegalStateException("unreachable: validated sortBy=" + sortBy);
        };
    }

    // ===================== 프로필 변경 (본인만) =====================

    @Transactional
    public UserResponse updateProfile(UUID requesterId, UUID targetUserId, String name, String profileImageUrl) {
        if (!requesterId.equals(targetUserId)) {
            throw new MoplException(UserErrorCode.ACCESS_DENIED);
        }
        findActiveUserOrThrow(targetUserId); // 존재/탈퇴 여부 확인
        userRepository.updateProfile(targetUserId, name, profileImageUrl);
        return UserResponse.from(findActiveUserOrThrow(targetUserId));
    }

    // ===================== 탈퇴 정책 =====================

    /** 본인 탈퇴. Soft Delete 처리하고, 이후 로그인/조회 대상에서 제외됨. */
    @Transactional
    public void withdraw(UUID requesterId, UUID targetUserId) {
        if (!requesterId.equals(targetUserId)) {
            throw new MoplException(UserErrorCode.ACCESS_DENIED);
        }
        findActiveUserOrThrow(targetUserId); // 존재/탈퇴 여부 확인
        userRepository.updateDeletedAt(targetUserId, LocalDateTime.now());
        tokenRedisService.deleteRefreshToken(targetUserId);
        tokenRedisService.invalidateTokensIssuedBefore(targetUserId, Instant.now());
    }

    // ===================== 잠금 정책 (어드민) =====================

    @Transactional
    public UserResponse updateLocked(UUID targetUserId, boolean locked) {
        findActiveUserOrThrow(targetUserId); // 존재/탈퇴 여부 확인
        userRepository.updateLocked(targetUserId, locked);
        if (locked) {
            tokenRedisService.deleteRefreshToken(targetUserId);
            tokenRedisService.invalidateTokensIssuedBefore(targetUserId, Instant.now());
        }
        return UserResponse.from(findActiveUserOrThrow(targetUserId));
    }

    // ===================== 권한 관리 (어드민) =====================

    @Transactional
    public UserResponse updateRole(UUID targetUserId, UserRole role) {
        findActiveUserOrThrow(targetUserId); // 존재/탈퇴 여부 확인
        userRepository.updateRole(targetUserId, role);
        tokenRedisService.deleteRefreshToken(targetUserId);
        tokenRedisService.invalidateTokensIssuedBefore(targetUserId, Instant.now());
        return UserResponse.from(findActiveUserOrThrow(targetUserId));
    }

    // ===================== 공통 =====================

    private User findActiveUserOrThrow(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new MoplException(UserErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. userId=" + userId));
    }
}