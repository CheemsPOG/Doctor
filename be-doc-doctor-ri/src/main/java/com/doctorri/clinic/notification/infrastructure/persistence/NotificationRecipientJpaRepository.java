package com.doctorri.clinic.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecipientJpaRepository extends JpaRepository<NotificationRecipientEntity, Long> {
  List<NotificationRecipientEntity> findByRecipientUserIdOrderByIdDesc(Long userId);

  Optional<NotificationRecipientEntity> findByIdAndRecipientUserId(Long id, Long userId);

  Optional<NotificationRecipientEntity> findByNotificationIdAndRecipientUserId(
      Long notificationId, Long recipientUserId);

  List<NotificationRecipientEntity> findByRecipientUserIdAndReadFalse(Long userId);
}
