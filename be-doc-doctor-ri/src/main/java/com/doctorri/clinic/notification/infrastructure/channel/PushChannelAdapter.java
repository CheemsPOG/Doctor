package com.doctorri.clinic.notification.infrastructure.channel;

import org.springframework.stereotype.Component;

@Component
public class PushChannelAdapter {

  private final PushSender pushSender;

  public PushChannelAdapter(PushSender pushSender) {
    this.pushSender = pushSender;
  }

  public String send(String deviceToken, String title, String body) {
    return pushSender.send(deviceToken, title, body);
  }

  public String providerName() {
    return pushSender.providerName();
  }
}
