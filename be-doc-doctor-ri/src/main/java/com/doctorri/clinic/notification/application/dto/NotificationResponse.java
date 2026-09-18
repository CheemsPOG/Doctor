package com.doctorri.clinic.notification.application.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String type,
    String title,
    String content,
    String referenceType,
    Long referenceId,
    boolean read,
    LocalDateTime readAt,
    Instant createdAt
) {}
