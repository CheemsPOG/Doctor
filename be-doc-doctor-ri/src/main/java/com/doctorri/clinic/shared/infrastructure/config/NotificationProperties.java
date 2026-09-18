package com.doctorri.clinic.shared.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doctorri.notification")
public record NotificationProperties(
    String channels,
    String fromEmail,
    String reminderHours
) {

  public NotificationProperties {
    if (reminderHours == null || reminderHours.isBlank()) {
      reminderHours = "24,2";
    }
  }

  public List<String> channelList() {
    if (channels == null || channels.isBlank()) {
      return List.of();
    }
    return Arrays.stream(channels.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  public List<Integer> reminderHourList() {
    return Arrays.stream(reminderHours.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Integer::parseInt)
        .toList();
  }
}
