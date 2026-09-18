package com.doctorri.clinic.obstetric.application.dto;

import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyVisitEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record PregnancyVisitResponse(
    Long id,
    Long pregnancyId,
    Long appointmentId,
    Integer gestationalWeek,
    BigDecimal weight,
    String bloodPressure,
    Integer fetalHeartRate,
    String doctorNote,
    LocalDateTime nextVisitAt,
    Instant createdAt
) {
  public static PregnancyVisitResponse from(PregnancyVisitEntity e) {
    return new PregnancyVisitResponse(
        e.getId(), e.getPregnancyId(), e.getAppointmentId(), e.getGestationalWeek(), e.getWeight(),
        e.getBloodPressure(), e.getFetalHeartRate(), e.getDoctorNote(), e.getNextVisitAt(), e.getCreatedAt());
  }
}
