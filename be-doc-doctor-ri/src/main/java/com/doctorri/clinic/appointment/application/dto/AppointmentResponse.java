package com.doctorri.clinic.appointment.application.dto;

import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import java.time.OffsetDateTime;

public record AppointmentResponse(
    Long id,
    String appointmentCode,
    Long patientId,
    Long doctorId,
    Long serviceId,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    AppointmentStatus status,
    String reason,
    boolean lateCancel
) {}
