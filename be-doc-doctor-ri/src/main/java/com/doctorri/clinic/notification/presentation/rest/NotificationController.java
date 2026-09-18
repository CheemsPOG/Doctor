package com.doctorri.clinic.notification.presentation.rest;

import com.doctorri.clinic.notification.application.dto.NotificationPreferenceResponse;
import com.doctorri.clinic.notification.application.dto.NotificationResponse;
import com.doctorri.clinic.notification.application.dto.RegisterPushDeviceRequest;
import com.doctorri.clinic.notification.application.dto.UpdateNotificationPreferenceRequest;
import com.doctorri.clinic.notification.application.usecase.NotificationApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class NotificationController {

  private final NotificationApplicationService notificationService;

  public NotificationController(NotificationApplicationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping("/notifications")
  public List<NotificationResponse> notifications() {
    return notificationService.myNotifications();
  }

  @PatchMapping("/notifications/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@PathVariable Long id) {
    notificationService.markRead(id);
  }

  @PatchMapping("/notifications/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllRead() {
    notificationService.markAllRead();
  }

  @GetMapping("/notification-preferences")
  public List<NotificationPreferenceResponse> preferences() {
    return notificationService.myPreferences();
  }

  @PutMapping("/notification-preferences")
  public NotificationPreferenceResponse updatePreferences(@Valid @RequestBody UpdateNotificationPreferenceRequest request) {
    return notificationService.updatePreference(
        request.notificationType(), request.inAppEnabled(), request.emailEnabled(), request.smsEnabled(), request.pushEnabled());
  }

  @PostMapping("/push-devices")
  @ResponseStatus(HttpStatus.CREATED)
  public void registerPushDevice(@Valid @RequestBody RegisterPushDeviceRequest request) {
    notificationService.registerPushDevice(request.deviceToken(), request.platform());
  }
}
