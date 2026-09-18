package com.doctorri.clinic.obstetric.application.usecase;

import com.doctorri.clinic.auth.domain.model.UserRole;
import com.doctorri.clinic.auth.infrastructure.security.AuthUserPrincipal;
import com.doctorri.clinic.obstetric.application.dto.CreatePregnancyRequest;
import com.doctorri.clinic.obstetric.application.dto.CreatePregnancyVisitRequest;
import com.doctorri.clinic.obstetric.application.dto.PregnancyResponse;
import com.doctorri.clinic.obstetric.application.dto.PregnancyVisitResponse;
import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyEntity;
import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyJpaRepository;
import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyVisitEntity;
import com.doctorri.clinic.obstetric.infrastructure.persistence.PregnancyVisitJpaRepository;
import com.doctorri.clinic.patient.application.usecase.PatientContext;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObstetricApplicationService {

  private final PregnancyJpaRepository pregnancies;
  private final PregnancyVisitJpaRepository visits;
  private final PatientContext patientContext;

  public ObstetricApplicationService(
      PregnancyJpaRepository pregnancies, PregnancyVisitJpaRepository visits, PatientContext patientContext) {
    this.pregnancies = pregnancies;
    this.visits = visits;
    this.patientContext = patientContext;
  }

  @Transactional
  public PregnancyResponse create(CreatePregnancyRequest request) {
    Long patientId = request.patientId() != null ? request.patientId() : patientContext.currentPatientId();
    PregnancyEntity entity = new PregnancyEntity();
    entity.setPatientId(patientId);
    entity.setPregnancyCode("PG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    entity.setLastMenstrualPeriod(request.lastMenstrualPeriod());
    entity.setEstimatedDueDate(request.estimatedDueDate());
    entity.setAssignedDoctorId(request.assignedDoctorId());
    pregnancies.save(entity);
    return PregnancyResponse.from(entity);
  }

  @Transactional(readOnly = true)
  public List<PregnancyResponse> list(Long patientIdParam) {
    AuthUserPrincipal principal = SecurityUtils.requireUser();
    Long patientId;
    if (principal.getRole() == UserRole.PATIENT) {
      patientId = patientContext.currentPatientId();
    } else {
      patientId = patientIdParam;
      if (patientId == null) {
        throw new DomainException("PATIENT_ID_REQUIRED", "Cần cung cấp patientId.");
      }
    }
    return pregnancies.findByPatientIdOrderByIdDesc(patientId).stream().map(PregnancyResponse::from).toList();
  }

  @Transactional
  public PregnancyVisitResponse addVisit(Long pregnancyId, CreatePregnancyVisitRequest request) {
    pregnancies.findById(pregnancyId)
        .orElseThrow(() -> new DomainException("PREGNANCY_NOT_FOUND", "Không tìm thấy hồ sơ thai kỳ."));
    PregnancyVisitEntity entity = new PregnancyVisitEntity();
    entity.setPregnancyId(pregnancyId);
    entity.setAppointmentId(request.appointmentId());
    entity.setGestationalWeek(request.gestationalWeek());
    entity.setWeight(request.weight());
    entity.setBloodPressure(request.bloodPressure());
    entity.setFetalHeartRate(request.fetalHeartRate());
    entity.setDoctorNote(request.doctorNote());
    entity.setNextVisitAt(request.nextVisitAt());
    visits.save(entity);
    return PregnancyVisitResponse.from(entity);
  }

  @Transactional(readOnly = true)
  public List<PregnancyVisitResponse> listVisits(Long pregnancyId) {
    if (pregnancies.findById(pregnancyId).isEmpty()) {
      throw new DomainException("PREGNANCY_NOT_FOUND", "Không tìm thấy hồ sơ thai kỳ.");
    }
    return visits.findByPregnancyIdOrderByCreatedAtDesc(pregnancyId).stream().map(PregnancyVisitResponse::from).toList();
  }
}
