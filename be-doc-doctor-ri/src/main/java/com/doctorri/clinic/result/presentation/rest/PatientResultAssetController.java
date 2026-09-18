package com.doctorri.clinic.result.presentation.rest;

import com.doctorri.clinic.patient.infrastructure.persistence.PatientJpaRepository;
import com.doctorri.clinic.result.infrastructure.persistence.ClinicalResultAssetEntity;
import com.doctorri.clinic.result.infrastructure.persistence.ClinicalResultAssetJpaRepository;
import com.doctorri.clinic.shared.domain.exception.DomainException;
import com.doctorri.clinic.shared.infrastructure.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/result-assets")
public class PatientResultAssetController {

  private final ClinicalResultAssetJpaRepository assets;
  private final PatientJpaRepository patients;

  public PatientResultAssetController(
      ClinicalResultAssetJpaRepository assets, PatientJpaRepository patients) {
    this.assets = assets;
    this.patients = patients;
  }

  @GetMapping
  public List<ResultAssetResponse> myAssets() {
    var user = SecurityUtils.requireUser();
    Long patientId = user.getPatientId();
    if (patientId == null) {
      patientId = patients.findByUserId(user.getUserId())
          .map(p -> p.getId())
          .orElseThrow(() -> new DomainException("FORBIDDEN", "Tài khoản không gắn bệnh nhân."));
    }
    return assets.findByPatientIdAndStatusOrderByCreatedAtDesc(patientId, "ACTIVE").stream()
        .map(ResultAssetResponse::from)
        .toList();
  }

  public record ResultAssetResponse(
      Long id,
      Long appointmentId,
      Long serviceId,
      String mediaType,
      String title,
      String caption,
      String url,
      String thumbnailUrl,
      Instant createdAt
  ) {
    static ResultAssetResponse from(ClinicalResultAssetEntity e) {
      return new ResultAssetResponse(
          e.getId(),
          e.getAppointmentId(),
          e.getServiceId(),
          e.getMediaType(),
          e.getTitle(),
          e.getCaption(),
          e.getUrl(),
          e.getThumbnailUrl(),
          e.getCreatedAt());
    }
  }
}
