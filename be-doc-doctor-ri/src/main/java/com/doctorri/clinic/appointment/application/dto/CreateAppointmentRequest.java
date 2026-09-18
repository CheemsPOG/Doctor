package com.doctorri.clinic.appointment.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateAppointmentRequest(
    Long patientId,
    @NotNull Long serviceId,
    Long doctorId,
    @NotNull OffsetDateTime startAt,
    String reason
) {}
