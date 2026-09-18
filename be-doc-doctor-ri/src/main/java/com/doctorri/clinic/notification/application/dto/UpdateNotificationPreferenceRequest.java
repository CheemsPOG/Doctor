package com.doctorri.clinic.notification.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNotificationPreferenceRequest(
    @NotBlank String notificationType,
    boolean inAppEnabled,
    boolean emailEnabled,
    boolean smsEnabled,
    boolean pushEnabled
) {}
