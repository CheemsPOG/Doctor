package com.doctorri.clinic.audit.application;

import com.doctorri.clinic.audit.infrastructure.persistence.AuditLogEntity;
import com.doctorri.clinic.audit.infrastructure.persistence.AuditLogJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogJpaRepository logs;
  public AuditService(AuditLogJpaRepository logs) { this.logs = logs; }

  @Transactional
  public void log(Long actorUserId, String action, String entityType, Long entityId, String detail) {
    AuditLogEntity e = new AuditLogEntity();
    e.setActorUserId(actorUserId);
    e.setAction(action);
    e.setEntityType(entityType);
    e.setEntityId(entityId);
    e.setDetail(detail);
    logs.save(e);
  }
}
