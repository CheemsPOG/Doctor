package com.doctorri.clinic.notification.infrastructure.outbox;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
  @Query("""
      select e from OutboxEventEntity e
      where e.status = 'PENDING' and e.availableAt <= :now
      order by e.id asc
      """)
  List<OutboxEventEntity> findReady(@Param("now") Instant now);

  @Query("""
      select e from OutboxEventEntity e
      where e.aggregateId = :appointmentId
        and e.status = 'PENDING'
        and e.eventType in ('APPOINTMENT_REMINDER_24H', 'APPOINTMENT_REMINDER_2H')
      """)
  List<OutboxEventEntity> findPendingReminders(@Param("appointmentId") Long appointmentId);
}
