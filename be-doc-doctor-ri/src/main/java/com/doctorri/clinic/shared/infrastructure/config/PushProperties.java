package com.doctorri.clinic.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "doctorri.push")
public record PushProperties(
    /** STUB (default) or FCM */
    String provider,
    String fcmCredentialsPath,
    String fcmProjectId
) {
  public PushProperties {
    if (provider == null || provider.isBlank()) {
      provider = "STUB";
    }
  }

  public boolean fcmEnabled() {
    return "FCM".equalsIgnoreCase(provider)
        && fcmCredentialsPath != null
        && !fcmCredentialsPath.isBlank();
  }
}
