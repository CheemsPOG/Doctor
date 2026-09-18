package com.doctorri.clinic.resource.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_bookings")
public class ResourceBookingEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "appointment_id", nullable = false) private Long appointmentId;
  @Column(name = "resource_type", nullable = false) private String resourceType;
  @Column(name = "resource_id", nullable = false) private Long resourceId;
  @Column(name = "start_at", nullable = false) private LocalDateTime startAt;
  @Column(name = "end_at", nullable = false) private LocalDateTime endAt;
  @Column(nullable = false) private String status;
  @Column(name = "hold_expires_at") private LocalDateTime holdExpiresAt;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public Long getAppointmentId() { return appointmentId; }
  public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
  public String getResourceType() { return resourceType; }
  public void setResourceType(String resourceType) { this.resourceType = resourceType; }
  public Long getResourceId() { return resourceId; }
  public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
  public LocalDateTime getStartAt() { return startAt; }
  public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
  public LocalDateTime getEndAt() { return endAt; }
  public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
  public void setHoldExpiresAt(LocalDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
}
