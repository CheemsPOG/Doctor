package com.doctorri.clinic.appointment.infrastructure.persistence;

import com.doctorri.clinic.appointment.domain.model.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {

  List<AppointmentEntity> findByPatientIdOrderByScheduledStartAtDesc(Long patientId);

  Optional<AppointmentEntity> findByAppointmentCode(String appointmentCode);

  List<AppointmentEntity> findByScheduledStartAtBetweenOrderByScheduledStartAtAsc(
      LocalDateTime start, LocalDateTime end);

  List<AppointmentEntity> findByDoctorIdAndScheduledStartAtBetweenOrderByScheduledStartAtAsc(
      Long doctorId, LocalDateTime start, LocalDateTime end);

  @Query("""
      select a from AppointmentEntity a
      where a.doctorId = :doctorId
        and a.status in :statuses
        and a.scheduledStartAt < :end
        and a.scheduledEndAt > :start
      """)
  List<AppointmentEntity> findOverlaps(
      @Param("doctorId") Long doctorId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      @Param("statuses") Collection<AppointmentStatus> statuses);
}
