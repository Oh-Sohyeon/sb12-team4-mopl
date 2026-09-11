package com.mopl.notification.repository;

import com.mopl.core.domain.notification.entity.Notification;
import com.mopl.core.domain.notification.entity.QNotification;
import com.mopl.core.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findByReceiverWithCursor(
      User receiver, UUID cursorId, LocalDateTime cursorCreatedAt, int size
  ) {
    QNotification notification = QNotification.notification;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(notification.receiver.eq(receiver));

    if (cursorId != null && cursorCreatedAt != null) {
      builder.and(
          notification.createdAt.lt(cursorCreatedAt)
              .or(notification.createdAt.eq(cursorCreatedAt)
                  .and(notification.id.lt(cursorId)))
      );
    }

    return queryFactory
        .selectFrom(notification)
        .where(builder)
        .orderBy(notification.createdAt.desc(), notification.id.desc())
        .limit(size)
        .fetch();
  }

  @Override
  public long countByReceiver(User receiver) {
    QNotification notification = QNotification.notification;
    Long count = queryFactory
        .select(notification.count())
        .from(notification)
        .where(notification.receiver.eq(receiver))
        .fetchOne();
    return count != null ? count : 0L;
  }
}