package com.doctorri.clinic.appointment.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusHistoryJpaRepository extends JpaRepository<AppointmentStatusHistoryEntity, Long> {
  List<AppointmentStatusHistoryEntity> findByAppointmentIdOrderByCreatedAtAsc(Long appointmentId);
}
