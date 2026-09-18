package com.doctorri.clinic.appointment.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RescheduleAppointmentRequest(
    @NotNull OffsetDateTime startAt,
    Long doctorId
) {}
