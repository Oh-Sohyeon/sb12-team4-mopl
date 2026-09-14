package com.mopl.notification.service;

import com.mopl.common.exception.notification.NotificationNotFoundException;
import com.mopl.common.exception.user.repository.UserRepository;
import com.mopl.core.common.dto.CursorResponse;
import com.mopl.core.domain.notification.entity.Notification;
import com.mopl.core.domain.user.entity.User;
import com.mopl.notification.dto.NotificationResponse;
import com.mopl.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  public CursorResponse<NotificationResponse> getNotifications(UUID userId, UUID cursorId, LocalDateTime cursorCreatedAt, int size) {
    User receiver = userRepository.findById(userId)
        .orElseThrow(NotificationNotFoundException::new);

    List<Notification> notifications = notificationRepository.findByReceiverWithCursor(receiver, cursorId, cursorCreatedAt, size+1);
    boolean hasNext = notifications.size() > size;
    List<Notification> content = hasNext ? notifications.subList(0, size) : notifications;

    List<NotificationResponse> data = content.stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());

    String nextCursor = hasNext ? content.get(content.size() - 1).getCreatedAt().toString() : null;
    String nextIdAfter = hasNext ? content.get(content.size() - 1).getId().toString() : null;

    return CursorResponse.of(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        notificationRepository.countByReceiver(receiver),  // 아래에서 추가할 메서드
        "createdAt",
        "DESCENDING"
    );
  }

  public void readNotification(UUID userId, UUID notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(NotificationNotFoundException::new);

    if (!notification.getReceiver().getId().equals(userId)) {
      throw new NotificationNotFoundException();
    }

    notification.markAsRead();
  }
}