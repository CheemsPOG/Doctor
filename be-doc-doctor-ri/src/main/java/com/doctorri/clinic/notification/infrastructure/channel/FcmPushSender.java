package com.doctorri.clinic.notification.infrastructure.channel;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FcmPushSender implements PushSender {

  private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);

  private final FirebaseMessaging messaging;

  public FcmPushSender(FirebaseMessaging messaging) {
    this.messaging = messaging;
  }

  @Override
  public String providerName() {
    return "FCM";
  }

  @Override
  public String send(String deviceToken, String title, String body) {
    try {
      Message message = Message.builder()
          .setToken(deviceToken)
          .setNotification(Notification.builder().setTitle(title).setBody(body).build())
          .putData("title", title == null ? "" : title)
          .putData("body", body == null ? "" : body)
          .build();
      String messageId = messaging.send(message);
      log.info("FCM sent token={} messageId={}", deviceToken, messageId);
      return messageId;
    } catch (FirebaseMessagingException ex) {
      log.warn("FCM send failed token={} code={}: {}", deviceToken, ex.getMessagingErrorCode(), ex.getMessage());
      throw new IllegalStateException("FCM_SEND_FAILED:" + ex.getMessagingErrorCode(), ex);
    }
  }
}
