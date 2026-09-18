package com.doctorri.clinic.patient.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientJpaRepository extends JpaRepository<PatientEntity, Long> {
  Optional<PatientEntity> findByUserId(Long userId);
}
