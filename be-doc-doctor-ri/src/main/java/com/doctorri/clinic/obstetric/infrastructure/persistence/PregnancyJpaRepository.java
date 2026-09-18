package com.doctorri.clinic.obstetric.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PregnancyJpaRepository extends JpaRepository<PregnancyEntity, Long> {
  List<PregnancyEntity> findByPatientIdOrderByIdDesc(Long patientId);
}
