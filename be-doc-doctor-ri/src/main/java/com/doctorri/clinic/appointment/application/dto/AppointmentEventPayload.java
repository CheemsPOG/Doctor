package com.doctorri.clinic.appointment.application.dto;

/** Outbox payload for APPOINTMENT_* events; carries recipient user ids so the
 * notification worker does not need to re-join appointment/patient/doctor tables. */
public record AppointmentEventPayload(
    Long appointmentId,
    String appointmentCode,
    Long patientUserId,
    Long doctorUserId,
    String startAt,
    String status,
    String reason
) {}
