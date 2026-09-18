package com.doctorri.clinic.notification.application.dto;

public record NotificationPreferenceResponse(
    String notificationType,
    boolean inAppEnabled,
    boolean emailEnabled,
    boolean smsEnabled,
    boolean pushEnabled
) {}
