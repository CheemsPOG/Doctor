package com.doctorri.clinic.patient.application.usecase;

import com.doctorri.clinic.auth.infrastructure.persistence.UserJpaRepository;
import com.doctorri.clinic.patient.application.dto.PatientProfileResponse;
import com.doctorri.clinic.patient.application.dto.UpdatePatientProfileRequest;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientEntity;
import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientProfileService {

  private final PatientJpaRepository patients;
  private final UserJpaRepository users;

  public PatientProfileService(PatientJpaRepository patients, UserJpaRepository users) {
    this.patients = patients;
    this.users = users;
  }

  @Transactional(readOnly = true)
  public PatientProfileResponse getMine() {
    return toResponse(requireMine());
  }

  @Transactional
  public PatientProfileResponse updateMine(UpdatePatientProfileRequest request) {
    PatientEntity patient = requireMine();
    patient.setFullName(request.fullName().trim());
    patient.setDateOfBirth(request.dateOfBirth());
    patient.setGender(blankToNull(request.gender()));
    patient.setPhone(blankToNull(request.phone()));
    patient.setEmail(blankToNull(request.email()));
    patient.setEmergencyContactName(blankToNull(request.emergencyContactName()));
    patient.setEmergencyContactPhone(blankToNull(request.emergencyContactPhone()));

    users.findById(patient.getUserId()).ifPresent(user -> {
      user.setFullName(patient.getFullName());
      if (patient.getPhone() != null) {
        user.setPhone(patient.getPhone());
      }
      if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
        // keep login email stable unless empty on user — only sync display phone/name
      }
      user.touch();
    });
    return toResponse(patient);
  }

  private PatientEntity requireMine() {
    var principal = SecurityUtils.requireUser();
    if (principal.getPatientId() != null) {
      return patients.findById(principal.getPatientId())
          .orElseThrow(() -> new DomainException("PATIENT_NOT_FOUND", "Không tìm thấy hồ sơ bệnh nhân."));
    }
    return patients.findByUserId(principal.getUserId())
        .orElseThrow(() -> new DomainException("PATIENT_NOT_FOUND", "Không tìm thấy hồ sơ bệnh nhân."));
  }

  private static PatientProfileResponse toResponse(PatientEntity p) {
    return new PatientProfileResponse(
        p.getId(),
        p.getUserId(),
        p.getPatientCode(),
        p.getFullName(),
        p.getDateOfBirth(),
        p.getGender(),
        p.getPhone(),
        p.getEmail(),
        p.getEmergencyContactName(),
        p.getEmergencyContactPhone());
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
