package com.doctorri.clinic.notification.infrastructure.channel;

import com.doctorri.clinic.notification.infrastructure.persistence.NotificationDeliveryEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.PushDeviceJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushChannel implements NotificationChannel {

  private static final Logger log = LoggerFactory.getLogger(PushChannel.class);

  private final PushDeviceJpaRepository pushDevices;
  private final PushSender pushSender;

  public PushChannel(PushDeviceJpaRepository pushDevices, PushSender pushSender) {
    this.pushDevices = pushDevices;
    this.pushSender = pushSender;
  }

  @Override
  public boolean supports(String channelCode) {
    return "PUSH".equalsIgnoreCase(channelCode);
  }

  @Override
  public void send(NotificationDeliveryEntity delivery, NotificationEntity notification, Long recipientUserId) {
    List<PushDeviceEntity> devices = pushDevices.findByUserIdAndActiveTrue(recipientUserId);
    delivery.setProvider(pushSender.providerName());
    if (devices.isEmpty()) {
      delivery.setStatus("SKIPPED");
      delivery.setErrorCode("NO_DEVICE");
      return;
    }
    PushDeviceEntity device = devices.get(0);
    delivery.setDestination(device.getDeviceToken());
    try {
      String messageId = pushSender.send(device.getDeviceToken(), notification.getTitle(), notification.getContent());
      delivery.setProviderMessageId(messageId);
      delivery.setStatus("SENT");
      delivery.setSentAt(LocalDateTime.now());
    } catch (RuntimeException ex) {
      log.warn("Push failed userId={}: {}", recipientUserId, ex.getMessage());
      delivery.setStatus("FAILED");
      delivery.setFailedAt(LocalDateTime.now());
      delivery.setErrorCode("PUSH_ERROR");
    }
  }
}
