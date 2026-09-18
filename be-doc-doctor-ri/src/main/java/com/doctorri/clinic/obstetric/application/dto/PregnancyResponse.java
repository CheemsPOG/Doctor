package com.doctorri.clinic.obstetric.application.dto;

import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyEntity;
import java.time.LocalDate;

public record PregnancyResponse(
    Long id,
    Long patientId,
    String pregnancyCode,
    LocalDate lastMenstrualPeriod,
    LocalDate estimatedDueDate,
    String pregnancyStatus,
    String riskLevel,
    Long assignedDoctorId
) {
  public static PregnancyResponse from(PregnancyEntity e) {
    return new PregnancyResponse(
        e.getId(), e.getPatientId(), e.getPregnancyCode(), e.getLastMenstrualPeriod(),
        e.getEstimatedDueDate(), e.getPregnancyStatus(), e.getRiskLevel(), e.getAssignedDoctorId());
  }
}
