package com.doctorri.clinic.notification.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPushDeviceRequest(
    @NotBlank String deviceToken,
    String platform
) {}
