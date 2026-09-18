package com.doctorri.clinic.admin.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertServiceRequest(
    @NotBlank @Size(max = 32) String code,
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 64) String category,
    @Size(max = 1024) String description,
    @Min(5) int durationMinutes,
    @Min(0) int bufferBeforeMinutes,
    @Min(0) int bufferAfterMinutes,
    boolean holdRoomOnBooking,
    boolean requiresUltrasound,
    String bookingPolicy,
    @NotBlank String status
) {}
