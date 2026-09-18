package com.doctorri.clinic.audit.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "actor_user_id") private Long actorUserId;
  @Column(nullable = false) private String action;
  @Column(name = "entity_type", nullable = false) private String entityType;
  @Column(name = "entity_id") private Long entityId;
  @Column(length = 2000) private String detail;
  @Column(name = "ip_address") private String ipAddress;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  public Long getId() { return id; }
  public Long getActorUserId() { return actorUserId; }
  public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getEntityType() { return entityType; }
  public void setEntityType(String entityType) { this.entityType = entityType; }
  public Long getEntityId() { return entityId; }
  public void setEntityId(Long entityId) { this.entityId = entityId; }
  public String getDetail() { return detail; }
  public void setDetail(String detail) { this.detail = detail; }
  public Instant getCreatedAt() { return createdAt; }
}
