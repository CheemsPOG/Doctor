package com.doctorri.clinic.obstetric.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PregnancyVisitJpaRepository extends JpaRepository<PregnancyVisitEntity, Long> {
  List<PregnancyVisitEntity> findByPregnancyIdOrderByCreatedAtDesc(Long pregnancyId);
}
