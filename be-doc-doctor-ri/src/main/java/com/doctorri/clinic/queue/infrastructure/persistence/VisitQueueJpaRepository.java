package com.doctorri.clinic.queue.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitQueueJpaRepository extends JpaRepository<VisitQueueEntity, Long> {

  Optional<VisitQueueEntity> findByAppointmentId(Long appointmentId);

  @Query("""
      select q from VisitQueueEntity q
      where q.checkedInAt >= :from and q.checkedInAt < :to
      order by q.priority asc, q.queueNumber asc
      """)
  List<VisitQueueEntity> findByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query("select coalesce(max(q.queueNumber), 0) from VisitQueueEntity q where q.checkedInAt >= :from and q.checkedInAt < :to")
  int maxQueueNumber(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
