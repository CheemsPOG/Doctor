package com.doctorri.clinic.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceEntity, Long> {
  List<NotificationPreferenceEntity> findByUserId(Long userId);

  Optional<NotificationPreferenceEntity> findByUserIdAndNotificationType(Long userId, String notificationType);
}
