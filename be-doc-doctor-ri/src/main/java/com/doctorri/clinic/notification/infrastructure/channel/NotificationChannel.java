package com.doctorri.clinic.notification.infrastructure.channel;

import com.doctorri.clinic.notification.infrastructure.persistence.NotificationDeliveryEntity;
import com.doctorri.clinic.notification.infrastructure.persistence.NotificationEntity;

public interface NotificationChannel {

  boolean supports(String channelCode);

  void send(NotificationDeliveryEntity delivery, NotificationEntity notification, Long recipientUserId);
}
