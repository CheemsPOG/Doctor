package com.doctorri.clinic.encounter.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterActivityJpaRepository extends JpaRepository<EncounterActivityEntity, Long> {
  List<EncounterActivityEntity> findByEncounterIdOrderByCreatedAtAsc(Long encounterId);
}
