package com.doctorri.clinic.result.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalResultAssetJpaRepository extends JpaRepository<ClinicalResultAssetEntity, Long> {
  List<ClinicalResultAssetEntity> findByPatientIdAndStatusOrderByCreatedAtDesc(Long patientId, String status);
}
