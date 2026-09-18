package com.doctorri.clinic.notification.infrastructure.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubPushSender implements PushSender {

  private static final Logger log = LoggerFactory.getLogger(StubPushSender.class);

  @Override
  public String providerName() {
    return "FCM_STUB";
  }

  @Override
  public String send(String deviceToken, String title, String body) {
    log.info("[PUSH-STUB] token={} title={} body={}", deviceToken, title, body);
    return "stub-" + System.currentTimeMillis();
  }
}
