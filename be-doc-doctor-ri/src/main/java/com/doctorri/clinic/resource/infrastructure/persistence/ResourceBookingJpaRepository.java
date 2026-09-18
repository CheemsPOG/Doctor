package com.doctorri.clinic.resource.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceBookingJpaRepository extends JpaRepository<ResourceBookingEntity, Long> {
  List<ResourceBookingEntity> findByAppointmentId(Long appointmentId);

  @Query("""
      select r from ResourceBookingEntity r
      where r.resourceType = :type and r.resourceId = :resourceId
        and r.status in ('HELD','CONFIRMED')
        and r.startAt < :end and r.endAt > :start
      """)
  List<ResourceBookingEntity> findOverlaps(
      @Param("type") String type,
      @Param("resourceId") Long resourceId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
