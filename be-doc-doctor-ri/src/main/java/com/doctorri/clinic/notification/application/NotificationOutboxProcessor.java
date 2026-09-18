package com.doctorri.clinic.notification.application;

import com.doctorri.clinic.auth.infrastructure.persistence.UserEntity;
import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.notification.infrastructure.channel.EmailChannelAdapter;
import com.doctorri.clinic.notification.infrastructure.channel.PushChannelAdapter;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventEntity;
import com.doctorri.clinic.notification.infrastructure.outbox.OutboxEventJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationDeliveryEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationDeliveryJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationRecipientEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationRecipientJpaRepository;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceJpaRepository;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.infrastructure.config.NotificationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationOutboxProcessor {

  private static final Logger log = LoggerFactory.getLogger(NotificationOutboxProcessor.class);

  private final OutboxEventJpaRepository outbox;
  private final NotificationJpaRepository notifications;
  private final NotificationRecipientJpaRepository recipients;
  private final NotificationDeliveryJpaRepository deliveries;
  private final PushDeviceJpaRepository devices;
  private final UserJpaRepository users;
  private final PatientJpaRepository patients;
  private final EmailChannelAdapter emailChannel;
  private final PushChannelAdapter pushChannel;
  private final NotificationProperties props;
  private final ObjectMapper mapper;

  public NotificationOutboxProcessor(
      OutboxEventJpaRepository outbox,
      NotificationJpaRepository notifications,
      NotificationRecipientJpaRepository recipients,
      NotificationDeliveryJpaRepository deliveries,
      PushDeviceJpaRepository devices,
      UserJpaRepository users,
      PatientJpaRepository patients,
      EmailChannelAdapter emailChannel,
      PushChannelAdapter pushChannel,
      NotificationProperties props,
      ObjectMapper mapper) {
    this.outbox = outbox;
    this.notifications = notifications;
    this.recipients = recipients;
    this.deliveries = deliveries;
    this.devices = devices;
    this.users = users;
    this.patients = patients;
    this.emailChannel = emailChannel;
    this.pushChannel = pushChannel;
    this.props = props;
    this.mapper = mapper;
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void process() {
    List<OutboxEventEntity> ready = outbox.findReady(Instant.now());
    for (OutboxEventEntity event : ready) {
      try {
        handle(event);
        event.setStatus("PROCESSED");
        event.setProcessedAt(Instant.now());
      } catch (Exception ex) {
        log.error("Outbox {} failed: {}", event.getId(), ex.getMessage());
        event.setRetryCount(event.getRetryCount() + 1);
        event.setAvailableAt(Instant.now().plusSeconds(60L * event.getRetryCount()));
        if (event.getRetryCount() >= 5) {
          event.setStatus("FAILED");
        }
      }
    }
  }

  void handle(OutboxEventEntity event) throws Exception {
    JsonNode payload = mapper.readTree(event.getPayload());
    Long patientId = payload.has("patientId") ? payload.get("patientId").asLong() : null;
    Long appointmentId = payload.path("appointmentId").asLong();
    Long userId = null;
    if (patientId != null) {
      userId = patients.findById(patientId).map(PatientEntity::getUserId).orElse(null);
    }
    if (userId == null) {
      // fallback: skip delivery but mark processed
      return;
    }
    UserEntity user = users.findById(userId).orElse(null);
    if (user == null) {
      return;
    }

    String title = switch (event.getEventType()) {
      case "APPOINTMENT_CREATED" -> "Đặt lịch thành công";
      case "APPOINTMENT_CANCELLED" -> "Lịch hẹn đã hủy";
      case "APPOINTMENT_RESCHEDULED" -> "Lịch hẹn đã đổi";
      case "APPOINTMENT_ATTENDANCE_CONFIRMED" -> "Đã xác nhận sẽ đến";
      case "APPOINTMENT_REMINDER_24H" -> "Nhắc lịch khám (còn 24 giờ)";
      case "APPOINTMENT_REMINDER_2H" -> "Nhắc lịch khám (còn 2 giờ)";
      default -> event.getEventType().startsWith("APPOINTMENT_REMINDER")
          ? "Nhắc lịch khám"
          : "Thông báo Doctor Ri";
    };
    String content = "Mã lịch: " + payload.path("code").asText("AP")
        + (payload.has("startAt") ? " — " + payload.path("startAt").asText() : "")
        + " (" + event.getEventType() + ")";

    NotificationEntity n = new NotificationEntity();
    n.setType(event.getEventType());
    n.setTitle(title);
    n.setContent(content);
    n.setReferenceType("APPOINTMENT");
    n.setReferenceId(appointmentId);
    notifications.save(n);

    NotificationRecipientEntity recipient = new NotificationRecipientEntity();
    recipient.setNotificationId(n.getId());
    recipient.setRecipientUserId(userId);
    recipients.save(recipient);

    Set<String> channels = Arrays.stream(props.channels().split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toSet());

    if (channels.contains("EMAIL") && user.getEmail() != null) {
      deliverEmail(n, user);
    }
    if (channels.contains("PUSH")) {
      deliverPush(n, userId);
    }
  }

  private void deliverEmail(NotificationEntity n, UserEntity user) {
    NotificationDeliveryEntity d = new NotificationDeliveryEntity();
    d.setNotificationId(n.getId());
    d.setRecipientUserId(user.getId());
    d.setChannel("EMAIL");
    d.setDestination(user.getEmail());
    d.setProvider("SMTP");
    d.setAttemptCount(1);
    try {
      emailChannel.send(props.fromEmail(), user.getEmail(), n.getTitle(), n.getContent());
      d.setStatus("SENT");
      d.setSentAt(LocalDateTime.now());
    } catch (Exception ex) {
      d.setStatus("FAILED");
      d.setFailedAt(LocalDateTime.now());
      d.setErrorCode("EMAIL_ERROR");
    }
    deliveries.save(d);
  }

  private void deliverPush(NotificationEntity n, Long userId) {
    var tokens = devices.findByUserIdAndActiveTrue(userId);
    if (tokens.isEmpty()) {
      NotificationDeliveryEntity d = new NotificationDeliveryEntity();
      d.setNotificationId(n.getId());
      d.setRecipientUserId(userId);
      d.setChannel("PUSH");
      d.setStatus("SKIPPED");
      d.setAttemptCount(0);
      d.setProvider(pushChannel.providerName());
      deliveries.save(d);
      return;
    }
    tokens.forEach(dev -> {
      NotificationDeliveryEntity d = new NotificationDeliveryEntity();
      d.setNotificationId(n.getId());
      d.setRecipientUserId(userId);
      d.setChannel("PUSH");
      d.setDestination(dev.getDeviceToken());
      d.setProvider(pushChannel.providerName());
      d.setAttemptCount(1);
      try {
        String messageId = pushChannel.send(dev.getDeviceToken(), n.getTitle(), n.getContent());
        d.setProviderMessageId(messageId);
        d.setStatus("SENT");
        d.setSentAt(LocalDateTime.now());
      } catch (RuntimeException ex) {
        d.setStatus("FAILED");
        d.setFailedAt(LocalDateTime.now());
        d.setErrorCode("PUSH_ERROR");
      }
      deliveries.save(d);
    });
  }
}
