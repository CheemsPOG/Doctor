package com.doctorri.clinic.notification.infrastructure.channel;

/** Abstraction over push providers so local/dev can stay on stub while prod uses FCM. */
public interface PushSender {

  /** Provider label stored on notification_deliveries.provider */
  String providerName();

  /**
   * Send a data+notification push to a device token.
   *
   * @return provider message id when available, otherwise null
   */
  String send(String deviceToken, String title, String body);
}
