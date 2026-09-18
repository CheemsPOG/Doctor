package com.doctorri.clinic.notification.infrastructure.config;

import com.doctorri.clinic.notification.infrastructure.channel.FcmPushSender;
import com.doctorri.clinic.notification.infrastructure.channel.PushSender;
import com.doctorri.clinic.notification.infrastructure.channel.StubPushSender;
import com.doctorri.clinic.shared.infrastructure.config.PushProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class PushConfig {

  private static final Logger log = LoggerFactory.getLogger(PushConfig.class);

  @Bean
  PushSender pushSender(PushProperties props, ResourceLoader resourceLoader) {
    if (!props.fcmEnabled()) {
      log.info("Push provider=STUB (set doctorri.push.provider=FCM + credentials to enable real FCM)");
      return new StubPushSender();
    }
    try {
      FirebaseMessaging messaging = initFirebase(props, resourceLoader);
      log.info("Push provider=FCM projectId={}", props.fcmProjectId());
      return new FcmPushSender(messaging);
    } catch (Exception ex) {
      log.error("Failed to init FCM, falling back to STUB: {}", ex.getMessage());
      return new StubPushSender();
    }
  }

  private static FirebaseMessaging initFirebase(PushProperties props, ResourceLoader resourceLoader)
      throws IOException {
    if (!FirebaseApp.getApps().isEmpty()) {
      return FirebaseMessaging.getInstance();
    }
    try (InputStream in = openCredentials(props.fcmCredentialsPath(), resourceLoader)) {
      FirebaseOptions.Builder builder = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(in));
      if (props.fcmProjectId() != null && !props.fcmProjectId().isBlank()) {
        builder.setProjectId(props.fcmProjectId());
      }
      FirebaseApp.initializeApp(builder.build());
    }
    return FirebaseMessaging.getInstance();
  }

  private static InputStream openCredentials(String path, ResourceLoader resourceLoader) throws IOException {
    if (path.startsWith("classpath:")) {
      Resource resource = resourceLoader.getResource(path);
      return resource.getInputStream();
    }
    return new FileInputStream(path);
  }
}
