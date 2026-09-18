package com.doctorri.clinic.encounter.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncounterJpaRepository extends JpaRepository<EncounterEntity, Long> {
  Optional<EncounterEntity> findByAppointmentId(Long appointmentId);
}
