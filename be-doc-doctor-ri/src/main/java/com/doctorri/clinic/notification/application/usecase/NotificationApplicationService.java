package com.doctorri.clinic.notification.application.usecase;

import com.doctorri.clinic.auth.infrastructure.security.AuthUserPrincipal;
import com.doctorri.clinic.notification.application.dto.NotificationPreferenceResponse;
import com.doctorri.clinic.notification.application.dto.NotificationResponse;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationPreferenceEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationPreferenceJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationRecipientEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationRecipientJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationApplicationService {

  public static final List<String> NOTIFICATION_TYPES = List.of(
      "APPOINTMENT_CREATED", "APPOINTMENT_CANCELLED", "APPOINTMENT_RESCHEDULED",
      "ATTENDANCE_CONFIRMED", "APPOINTMENT_REMINDER", "QUEUE", "SYSTEM");

  private final NotificationJpaRepository notifications;
  private final NotificationRecipientJpaRepository recipients;
  private final NotificationPreferenceJpaRepository preferences;
  private final PushDeviceJpaRepository pushDevices;

  public NotificationApplicationService(
      NotificationJpaRepository notifications,
      NotificationRecipientJpaRepository recipients,
      NotificationPreferenceJpaRepository preferences,
      PushDeviceJpaRepository pushDevices) {
    this.notifications = notifications;
    this.recipients = recipients;
    this.preferences = preferences;
    this.pushDevices = pushDevices;
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> myNotifications() {
    Long userId = SecurityUtils.requireUser().getUserId();
    List<NotificationRecipientEntity> recipientRows = recipients.findByRecipientUserIdOrderByIdDesc(userId);
    if (recipientRows.isEmpty()) {
      return List.of();
    }
    Map<Long, NotificationEntity> byId = notifications
        .findAllById(recipientRows.stream().map(NotificationRecipientEntity::getNotificationId).toList())
        .stream()
        .collect(Collectors.toMap(NotificationEntity::getId, Function.identity()));
    return recipientRows.stream()
        .map(r -> toResponse(r, byId.get(r.getNotificationId())))
        .filter(r -> r != null)
        .toList();
  }

  @Transactional
  public void markRead(Long notificationId) {
    Long userId = SecurityUtils.requireUser().getUserId();
    NotificationRecipientEntity recipient =
        recipients.findByNotificationIdAndRecipientUserId(notificationId, userId)
            .orElseThrow(() -> new DomainException("NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo."));
    recipient.setRead(true);
    recipient.setReadAt(LocalDateTime.now());
  }

  @Transactional
  public void markAllRead() {
    Long userId = SecurityUtils.requireUser().getUserId();
    LocalDateTime now = LocalDateTime.now();
    recipients.findByRecipientUserIdAndReadFalse(userId).forEach(r -> {
      r.setRead(true);
      r.setReadAt(now);
    });
  }

  @Transactional(readOnly = true)
  public List<NotificationPreferenceResponse> myPreferences() {
    Long userId = SecurityUtils.requireUser().getUserId();
    Map<String, NotificationPreferenceEntity> existing = preferences.findByUserId(userId).stream()
        .collect(Collectors.toMap(NotificationPreferenceEntity::getNotificationType, Function.identity()));
    return NOTIFICATION_TYPES.stream()
        .map(type -> {
          NotificationPreferenceEntity e = existing.get(type);
          if (e == null) {
            return new NotificationPreferenceResponse(type, true, true, false, true);
          }
          return new NotificationPreferenceResponse(
              type, e.isInAppEnabled(), e.isEmailEnabled(), e.isSmsEnabled(), e.isPushEnabled());
        })
        .toList();
  }

  @Transactional
  public NotificationPreferenceResponse updatePreference(
      String notificationType, boolean inApp, boolean email, boolean sms, boolean push) {
    Long userId = SecurityUtils.requireUser().getUserId();
    NotificationPreferenceEntity entity = preferences.findByUserIdAndNotificationType(userId, notificationType)
        .orElseGet(() -> {
          NotificationPreferenceEntity created = new NotificationPreferenceEntity();
          created.setUserId(userId);
          created.setNotificationType(notificationType);
          return created;
        });
    entity.setInAppEnabled(inApp);
    entity.setEmailEnabled(email);
    entity.setSmsEnabled(sms);
    entity.setPushEnabled(push);
    preferences.save(entity);
    return new NotificationPreferenceResponse(notificationType, inApp, email, sms, push);
  }

  @Transactional
  public void registerPushDevice(String deviceToken, String platform) {
    AuthUserPrincipal principal = SecurityUtils.requireUser();
    PushDeviceEntity device = pushDevices.findByDeviceToken(deviceToken).orElseGet(PushDeviceEntity::new);
    device.setUserId(principal.getUserId());
    device.setDeviceToken(deviceToken);
    device.setPlatform(platform != null && !platform.isBlank() ? platform : "WEB");
    device.setActive(true);
    pushDevices.save(device);
  }

  private NotificationResponse toResponse(NotificationRecipientEntity recipient, NotificationEntity notification) {
    if (notification == null) {
      return null;
    }
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getContent(),
        notification.getReferenceType(),
        notification.getReferenceId(),
        recipient.isRead(),
        recipient.getReadAt(),
        notification.getCreatedAt());
  }
}
