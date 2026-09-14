package com.mopl.notification.service;

import com.mopl.common.exception.notification.NotificationNotFoundException;
import com.mopl.common.exception.user.repository.UserRepository;
import com.mopl.core.common.dto.CursorResponse;
import com.mopl.core.common.enums.NotificationLevel;
import com.mopl.core.domain.notification.entity.Notification;
import com.mopl.core.domain.user.entity.User;
import com.mopl.notification.dto.NotificationResponse;
import com.mopl.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Nested
  @DisplayName("알림 목록 조회")
  class GetNotifications {

    @Test
    @DisplayName("성공 - 유저의 알림 목록을 커서 기준으로 조회한다")
    void 알림_목록_조회_성공() {
      // given
      UUID userId = UUID.randomUUID();
      User user = mock(User.class);
      given(user.getId()).willReturn(userId);

      Notification notification = mock(Notification.class);
      given(notification.getId()).willReturn(UUID.randomUUID());
      given(notification.getCreatedAt()).willReturn(LocalDateTime.now());
      given(notification.getReceiver()).willReturn(user);
      given(notification.getTitle()).willReturn("팔로우 알림");
      given(notification.getContent()).willReturn("OO님이 팔로우했습니다.");
      given(notification.getLevel()).willReturn(NotificationLevel.INFO);

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(notificationRepository.findByReceiverWithCursor(user, null, null, 21))
          .willReturn(List.of(notification));
      given(notificationRepository.countByReceiver(user)).willReturn(1L);

      // when
      CursorResponse<NotificationResponse> response =
          notificationService.getNotifications(userId, null, null, 20);

      // then
      assertThat(response.data()).hasSize(1);
      assertThat(response.data().get(0).title()).isEqualTo("팔로우 알림");
      assertThat(response.hasNext()).isFalse();
      assertThat(response.totalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 유저면 예외가 발생한다")
    void 알림_목록_조회_실패_유저_없음() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() ->
          notificationService.getNotifications(userId, null, null, 20)
      ).isInstanceOf(NotificationNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("알림 생성")
  class CreateNotification {

    @Test
    @DisplayName("성공 - 알림이 정상적으로 생성되어 저장된다")
    void 알림_생성_성공() {
      // given
      User receiver = mock(User.class);

      // when
      notificationService.createNotification(
          receiver, "팔로우 알림", "OO님이 팔로우했습니다.", NotificationLevel.INFO
      );

      // then
      then(notificationRepository).should().save(any());
    }
  }
}