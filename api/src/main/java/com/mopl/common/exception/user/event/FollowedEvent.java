package com.mopl.common.exception.user.event;

import java.util.UUID;

/**
 * 팔로우가 발생했을 때 발행되는 애플리케이션 이벤트.
 * notification 도메인에서 구독해 "OO님이 회원님을 팔로우했습니다" 같은 알림을 발행하는 데 사용.
 *
 * 필드 순서는 팔로우 담당자 요청 코드(eventPublisher.publishEvent(new FollowedEvent(followeeId, followerId)))
 * 그대로 맞춤: 팔로우 당한 사람(followeeId) -> 팔로우 한 사람(followerId)
 */
public record FollowedEvent(
        UUID followeeId,
        UUID followerId
) {
}