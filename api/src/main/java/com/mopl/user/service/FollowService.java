package com.mopl.user.service;

import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.common.exception.user.event.FollowedEvent;
import com.mopl.core.domain.user.entity.User;
import com.mopl.user.dto.FollowListResponse;
import com.mopl.user.dto.UserResponse;
import com.mopl.user.repository.FollowRepository;
import com.mopl.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new MoplException(UserErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
        User follower = findActiveUserOrThrow(followerId);
        User followee = findActiveUserOrThrow(followeeId);

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new MoplException(UserErrorCode.ALREADY_FOLLOWING);
        }

        followRepository.save(new com.mopl.core.domain.user.entity.Follow(follower, followee));
        eventPublisher.publishEvent(new FollowedEvent(followeeId, followerId));
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        if (!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new MoplException(UserErrorCode.FOLLOW_NOT_FOUND);
        }
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    public FollowListResponse getFollowers(UUID followeeId, int page, int size) {
        findActiveUserOrThrow(followeeId);
        Page<User> result = followRepository.findFollowers(followeeId, PageRequest.of(page, size));
        return toResponse(result);
    }

    public FollowListResponse getFollowing(UUID followerId, int page, int size) {
        findActiveUserOrThrow(followerId);
        Page<User> result = followRepository.findFollowing(followerId, PageRequest.of(page, size));
        return toResponse(result);
    }

    private FollowListResponse toResponse(Page<User> result) {
        return new FollowListResponse(
                result.getContent().stream().map(UserResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    private User findActiveUserOrThrow(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new MoplException(UserErrorCode.USER_NOT_FOUND, "존재하지 않는 사용자입니다. userId=" + userId));
    }
}