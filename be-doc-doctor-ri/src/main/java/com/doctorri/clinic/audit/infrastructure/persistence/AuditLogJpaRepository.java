package com.doctorri.clinic.audit.infrastructure.persistence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
  List<AuditLogEntity> findTop100ByOrderByCreatedAtDesc();
}
