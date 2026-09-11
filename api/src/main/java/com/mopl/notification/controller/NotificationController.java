package com.mopl.notification.controller;

import com.mopl.core.common.dto.CursorResponse;
import com.mopl.notification.dto.NotificationResponse;
import com.mopl.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorResponse<NotificationResponse>> getNotifications(
      @RequestParam UUID userId,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) LocalDateTime cursorCreatedAt,
      @RequestParam(defaultValue = "20") int size
  ) {
    CursorResponse<NotificationResponse> notifications = notificationService.getNotifications(userId, cursorId, cursorCreatedAt, size);
    return ResponseEntity.ok(notifications);

  }

//  @DeleteMapping("/{notificationId")
//  public ResponseEntity<Void> deleteNotification(@PathVariable UUID notificationId) {
//
//  }

}
