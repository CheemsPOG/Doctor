package com.doctorri.clinic.patient.application.usecase;

import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Component;

@Component
public class PatientContext {

  private final PatientJpaRepository patients;

  public PatientContext(PatientJpaRepository patients) {
    this.patients = patients;
  }

  public Long currentPatientId() {
    var principal = SecurityUtils.requireUser();
    if (principal.getPatientId() != null) {
      return principal.getPatientId();
    }
    return patients.findByUserId(principal.getUserId())
        .map(PatientEntity::getId)
        .orElseThrow(() -> new DomainException("PATIENT_NOT_FOUND", "Không tìm thấy hồ sơ bệnh nhân."));
  }
}
